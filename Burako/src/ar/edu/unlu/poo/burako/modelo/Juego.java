package ar.edu.unlu.poo.burako.modelo;

import java.util.ArrayList;
import java.util.Collections;

public class Juego implements JuegoMostrable {
    private final ArrayList<Ficha> juego;
    private Juego_tipo tipo;

    public Juego(ArrayList<Ficha> fichas) throws Exception {
        if (fichas.size() < 3 || fichas.size() > 13) {
            throw new Exception("No se pudo crear el juego: cantidad de fichas no válida");
        }
        if (fichas.size() < 7) {
            if (isEscalera(fichas)) {
                this.juego = fichas;
                this.tipo = Juego_tipo.Escalera;
            } else if (isPierna(fichas)) {
                this.juego = fichas;
                this.tipo = Juego_tipo.Pierna;
            } else {
                throw new Exception("No se pudo crear el juego: no válido para tamaño de 3 a 6 fichas");
            }
        } else {
            if (isEscalera(fichas)||isPierna(fichas)) {
                this.juego = fichas;
                if(isPura_escalera(fichas)){
                    this.tipo = Juego_tipo.Canasta_pura_escalera;
                }else if(isPura_piena(fichas)){
                    this.tipo = Juego_tipo.Canasta_pura_pierna;
                }else if(isEscalera(fichas)){
                    this.tipo = Juego_tipo.Canasta_inpura_escalera;
                } else if (isPierna(fichas)) {
                    this.tipo = Juego_tipo.Canasta_inpura_pierna;
                }
            } else {
                throw new Exception("No se pudo crear el juego: no válido para tamaño de 7 o más fichas");
            }
        }
    }

    private boolean isPura_escalera(ArrayList<Ficha> fichas) {
        if(isEscalera(fichas)){
            if(fichas.stream().anyMatch(f -> f.getNum() == Ficha_Num.Comodin )){
                return false;
            } else if (fichas.stream().anyMatch(f -> f.getNum() == Ficha_Num._2_ )) {
                Ficha f;
                for (int i = 0; i < fichas.size(); i++) {
                    f=fichas.get(i);
                    if(f.getNum()==Ficha_Num._2_){
                        i++;
                        f=fichas.get(i);
                        return f.getNum() == Ficha_Num._3_;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean isPura_piena(ArrayList<Ficha> fichas) {
        if(isPierna(fichas)){
            if(fichas.stream().anyMatch(f -> f.getNum() == Ficha_Num.Comodin )){
                return false;
            } else if (fichas.stream().anyMatch(f -> f.getNum() == Ficha_Num._2_ )) {
                for (int i = 0; i < fichas.size(); i++) {
                    Ficha f=fichas.get(i);
                    if(f.getNum()==Ficha_Num._2_&&fichas.get(i++).getNum()==Ficha_Num._2_){
                        return true;
                    }
                }
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean isPierna(ArrayList<Ficha> fichas) {
        boolean ret=true;
        Ficha f=fichas.getFirst();
        Ficha_Num nAnt;
        if (isComodin(f)) {
            nAnt = fichas.get(1).getNum();
            if (nAnt == Ficha_Num.Comodin) {
                return false;
            }
        } else {
            nAnt = f.getNum();
        }
        for (int i = 1; i < fichas.size(); i++) {
            f = fichas.get(i);
            if (f.getNum() != nAnt) {
                if (!isComodin(f)) {
                    ret = false;
                }
            }
        }
        return ret;
    }

    private boolean isEscalera(ArrayList<Ficha> fichas) {
        Ficha f=fichas.getFirst();
        Ficha_Num num;
        Ficha_Color color = f.getColor();
        int idx=1;
        if (isComodin(f)) {
            num = fichas.get(idx).getNum();
            color= fichas.get(idx).getColor();
            idx++;
            if (num == Ficha_Num.Comodin||num == Ficha_Num._2_) {
                return false;
            }
        } else {
            num = f.getNum();
        }
        for ( int i=idx ; i < fichas.size(); i++) {
            f = fichas.get(i);
            if (isComodin(f)) {
                num = Ficha.fichaNumSig(num,false);
                continue;
            }
            if (f.getColor() != color || f.getNum() != Ficha.fichaNumSig(num, false)) {
                return false;
            }
            num = f.getNum();
        }
        return true;
    }

    public void agregar(Ficha ficha, int pos) throws Exception {
        if (pos < 0  || pos > this.juego.size() + 1) {
            // Verifica que la posición sea válida
            throw new Exception("La posición no coresponde con el juego");
        }
        if (isValido(ficha, pos - 1)) {
            if(pos>=1&&pos<this.juego.size()&&(tipo==Juego_tipo.Escalera  || tipo==Juego_tipo.Canasta_inpura_escalera)&&isComodin(juego.get(pos-1))){
                this.juego.add(this.juego.size(),ficha);
                Collections.swap(this.juego, pos-1,this.juego.size()-1);
            }else {this.juego.add(pos - 1, ficha);}
            if (this.juego.size() >= 7) {
                cambioTipo();
            }
        }else {throw new Exception("Ficha invalida");}
    }

    private void cambioTipo() {
        if(isPura_escalera(this.juego)){
            this.tipo = Juego_tipo.Canasta_pura_escalera;
        }else if(isPura_piena(this.juego)){
            this.tipo = Juego_tipo.Canasta_pura_pierna;
        }else if(isEscalera(this.juego)){
            this.tipo = Juego_tipo.Canasta_inpura_escalera;
        } else if (isPierna(this.juego)) {
            this.tipo = Juego_tipo.Canasta_inpura_pierna;
        }
    }

    private boolean isValido(Ficha ficha, int pos) {
        if (this.tipo == Juego_tipo.Escalera) {
            return validarEscalera(ficha, pos);
        } else if (this.tipo == Juego_tipo.Pierna) {
            return validarPierna(ficha, pos);
        }
        return false;
    }

    private boolean validarEscalera(Ficha ficha, int pos) {
        if (pos > 0 && pos < this.juego.size() + 1) {
            Ficha anterior;
            if(isComodin(this.juego.get(pos - 1))){
                anterior = this.juego.get(pos - 2);
            }else {
                anterior = this.juego.get(pos - 1);
            }
            boolean f= anterior.getColor() == ficha.getColor() && Ficha.fichaNumSig(anterior.getNum(), false) == ficha.getNum();
            return f|| isComodin(ficha);
        } else if (pos == 0 && isComodin(this.juego.getFirst())) {
            Ficha siguiente = this.juego.get(1);
            return siguiente.getColor() == ficha.getColor() && Ficha.fichaNumSig(ficha.getNum(), false) == siguiente.getNum();
        }else if(pos == 0){
            Ficha siguiente = this.juego.getFirst();
            boolean f=siguiente.getColor() == ficha.getColor() && Ficha.fichaNumSig(ficha.getNum(), false) == siguiente.getNum();
            return f||isComodin(ficha);
        }
        return false;
    }

    private boolean validarPierna(Ficha ficha, int pos) {
        if (pos >= 0 && pos < this.juego.size() +1 && isComodin(juego.getFirst())) {
            Ficha referencia = this.juego.get(1);
            return referencia.getNum() == ficha.getNum() || isComodin(ficha);
        }else if(pos >= 0 && pos < this.juego.size() +1){
            Ficha referencia = this.juego.getFirst();
            return referencia.getNum() == ficha.getNum() || isComodin(ficha);
        }
        return false;
    }

    private boolean isComodin(Ficha ficha){
        return  ficha.getNum() == Ficha_Num.Comodin || ficha.getNum() == Ficha_Num._2_;
    }

    @Override
    public ArrayList<FichaMostrable> getJuego() {
        ArrayList<FichaMostrable> juegoMostrable = new ArrayList<>();
        for (Ficha ficha : this.juego) {
            juegoMostrable.add((FichaMostrable) ficha);
        }
        return juegoMostrable;
    }

    public Juego_tipo getTipo() {
        return tipo;
    }

    public int getPuntaje() {
        int puntos = 0;
        for (Ficha f : this.juego) {
            puntos += f.getValor();
        }
        if (tipo == Juego_tipo.Canasta_pura_escalera || tipo == Juego_tipo.Canasta_pura_pierna) {
            puntos += 200;
        } else if (tipo == Juego_tipo.Canasta_inpura_escalera || tipo == Juego_tipo.Canasta_inpura_pierna) {
            puntos += 100;
        }
        return puntos;
    }
}

