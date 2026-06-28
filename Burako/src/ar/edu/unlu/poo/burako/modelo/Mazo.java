package ar.edu.unlu.poo.burako.modelo;

import java.awt.*;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ar.edu.unlu.poo.burako.modelo.Ficha_Color.*;

import java.util.Collections;
public class Mazo {
    private ArrayList<Ficha> mazo;

    public Mazo(){
        this.mazo= new ArrayList<>();
        Ficha_Color color = Rojo;
        Ficha_Num num = Ficha_Num._1_;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 13; j++) {
                this.mazo.add(new Ficha(color,num));
                this.mazo.add(new Ficha(color,num));
                num=Ficha.fichaNumSig(num,false);
            }
            color=Ficha.fichaColorSig(color);
        }
        this.mazo.add(new Ficha(Negro,Ficha_Num.Comodin));
        this.mazo.add(new Ficha(Negro,Ficha_Num.Comodin));
        this.mesclar();
    }



    private void mesclar(){
        Collections.shuffle(this.mazo); // Desordena la lista
    }


    public ArrayList<Ficha> sacar(int cant) {
        ArrayList<Ficha> remazo=new ArrayList<>(this.mazo.subList(0, cant ));
        for(Ficha f: remazo){
            this.mazo.remove(f);
        }
        return remazo;
    }

    public Ficha sacarFicha() {
        return this.mazo.removeFirst();
    }
}
