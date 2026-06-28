package ar.edu.unlu.poo.burako.modelo;

import java.util.ArrayList;

public class Atril {
    private ArrayList<Ficha> atril;

    public Atril(ArrayList<Ficha> atril) {
        this.atril = atril;
    }

    public void agregar(Ficha ficha) {
        this.atril.add(ficha);
    }
    public void agregar(ArrayList<Ficha> fichas) {
        for(Ficha f:fichas){
            agregar(f);
        }
    }
    public ArrayList<Ficha> ver(int[] pos) throws Exception {
        var ret = new ArrayList<Ficha>();
        for (int p : pos) {
            if (p < 1 || p > atril.size()) {
                throw new Exception("La posicion " + p + " no existe en el atril");
            }
            ret.add(this.atril.get(p-1));
        }
        return ret;
    }
    public void sacar(ArrayList<Ficha> listafichas) throws Exception {
        boolean b;
        for(Ficha f: listafichas){
            b=atril.remove(f);
            if(!b){
                throw new Exception("no se pudo sacar a todas las fichas deceadas");
            }
        }
    }
    public ArrayList<FichaMostrable> get() {
        ArrayList<FichaMostrable> atril=new ArrayList<>();
        for(Ficha f: this.atril){
            atril.add((FichaMostrable) f);
        }
        return atril;
    }



}
