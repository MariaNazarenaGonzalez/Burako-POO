package ar.edu.unlu.poo.burako.modelo;

import java.util.ArrayList;

public class Pozo {
    private ArrayList<Ficha> pozo;

    public Pozo(){
        this.pozo=new ArrayList<>();
    }

    public ArrayList<Ficha> tomar() {
        ArrayList<Ficha> f=this.pozo;
        this.pozo=new ArrayList<>();
        return f;
    }

    public void agregar(Ficha ficha) throws Exception {
        boolean b=this.pozo.add(ficha);
        if(!b){throw new Exception("ERROR Pozo.agregar(Ficha ficha);");}
    }

    public ArrayList<FichaMostrable> get() {
        ArrayList<FichaMostrable> pozo=new ArrayList<>();
        for(Ficha f: this.pozo){
            pozo.add((FichaMostrable) f);
        }
        return pozo;
    }
}
