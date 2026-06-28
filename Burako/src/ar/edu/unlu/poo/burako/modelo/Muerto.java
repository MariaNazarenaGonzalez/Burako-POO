package ar.edu.unlu.poo.burako.modelo;

import java.util.ArrayList;

public class Muerto {
    private ArrayList<Ficha> muerto;

    public Muerto(ArrayList<Ficha> muerto) {
        this.muerto= muerto;
    }

    public ArrayList<Ficha> tomar(){
        if(!muerto.isEmpty()) return this.muerto;
        else return null;
    }
}
