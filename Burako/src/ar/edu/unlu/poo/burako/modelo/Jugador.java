package ar.edu.unlu.poo.burako.modelo;

import java.util.*;

public class Jugador {
    //private int puntos;
    private final Atril atril;
    private final ArrayList<Juego> Juegos;
    private boolean yaTomoMuerto = false;
    private final Burako burako;
    private String nombre;


    public Jugador(ArrayList<Ficha> atril, Burako burako) {
        this.atril= new Atril(atril);
        this.Juegos =new ArrayList<>();
        this.burako = burako;
        this.nombre = "";
    }

    public boolean yaTomoMuerto() {
        return yaTomoMuerto;
    }

    public void setYaTomoMuerto(boolean yaTomoMuerto) {
        this.yaTomoMuerto = yaTomoMuerto;
    }


    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(String nombreJugador) {
        this.nombre=nombreJugador;
    }



    public void bajarJuego(int[] listafichas) throws Exception {
        if(listafichas.length<3){throw new Exception("ERROR FICHAS MENORES A 3");}
        ArrayList<Ficha> f=atril.ver(listafichas);
        Juego j = new Juego(f);
        this.atril.sacar(f);
        this.Juegos.add(j);
    }

    public void apoyarJuego(int ficha, int pos,int juego) throws Exception {
        Ficha f=this.verAtril(ficha);
        this.Juegos.get(juego - 1).agregar(f,pos);
        this.sacarAtril(f);
    }

    public ArrayList<JuegoMostrable> getJugadas() {
        ArrayList<JuegoMostrable> juegosMostrable = new ArrayList<>();
        for (Juego juego : this.Juegos) {
            juegosMostrable.add((JuegoMostrable) juego);
        }
        return juegosMostrable;
    }

    public int cantJuegos() {
        return this.Juegos.size();
    }

    public boolean tieneCanasta() {
        for (Juego j : Juegos) {
            Juego_tipo tipo = j.getTipo();
            if (tipo == Juego_tipo.Canasta_pura_escalera || 
                tipo == Juego_tipo.Canasta_pura_pierna || 
                tipo == Juego_tipo.Canasta_inpura_escalera || 
                tipo == Juego_tipo.Canasta_inpura_pierna) {
                return true;
            }
        }
        return false;
    }



    public void agregarAtril(ArrayList<Ficha> fichas) {
        this.atril.agregar(fichas);
    }

    public void agregarAtril(Ficha ficha) {
        this.atril.agregar(ficha);
    }

    public Ficha verAtril(int ficha) throws Exception {
        int[] pos={ficha};
        ArrayList<Ficha> fichas = atril.ver(pos);
        if (fichas.isEmpty()) {
            throw new Exception("La ficha seleccionada no existe en el atril");
        }
        return fichas.getFirst();
    }

    public void sacarAtril(Ficha f) throws Exception {
        ArrayList<Ficha> ficha = new ArrayList<>();
        ficha.add(f);
        atril.sacar(ficha);
    }

    public ArrayList<FichaMostrable> getAtril() {
        return this.atril.get();
    }

    public int calcularPuntaje(boolean corto) {
        int total = 0;
        boolean tieneCanasta = tieneCanasta();

        // Sumar juegos bajados
        int puntosBajados = 0;
        for (Juego j : Juegos) {
            puntosBajados += j.getPuntaje();
        }

        // Fichas en el atril (siempre restan)
        int puntosAtril = 0;
        for (FichaMostrable f : getAtril()) {
            // Casting safe as Atril stores Ficha objects
            puntosAtril += ((Ficha) f).getValor();
        }

        if (!tieneCanasta) {
            // Si no tiene canasta, lo bajado también resta
            total = -(puntosBajados + puntosAtril);
        } else {
            total = puntosBajados - puntosAtril;
        }

        if (corto) {
            total += 100; // Bono por cerrar
        }

        if (yaTomoMuerto) {
            total += 100; // Bono por tomar el muerto
        } else {
            total -= 100; // Penalización por NO tomar el muerto
        }

        return total;
    }
}
