package ar.edu.unlu.poo.burako.modelo;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static ar.edu.unlu.poo.burako.modelo.Ficha_Color.Rojo;
import static ar.edu.unlu.poo.burako.modelo.Ficha_Color.values;

public class Ficha implements FichaMostrable{
    private Ficha_Num num;
    private Ficha_Color color;

    public Ficha(Ficha_Color color, Ficha_Num num) {
        this.color=color;
        this.num=num;
    }

    @Override
    public Ficha_Color getColor() {
        return this.color;
    }

    @Override
    public Ficha_Num getNum() {
        return this.num;
    }

    public int getValor() {
        switch (this.num) {
            case _1_:
                return 15;
            case _2_:
                return 20;
            case Comodin:
                return 50;
            case _3_:
            case _4_:
            case _5_:
            case _6_:
            case _7_:
                return 5;
            case _8_:
            case _9_:
            case _10_:
            case _11_:
            case _12_:
            case _13_:
                return 10;
            default:
                return 0;
        }
    }

    public static Ficha_Num fichaNumSig(Ficha_Num num,boolean comodin) {
        ArrayList<Ficha_Num> lista = new ArrayList<>(Arrays.asList(Ficha_Num.values()));
        int i=0;
        for(Ficha_Num n: lista){
            i++;
            if(num==n){break;}
        }
        if(comodin){
            if(i< lista.size()) {
                num = lista.get(i);
                return num;
            }else return Ficha_Num._1_;
        }else {
            if(i< (lista.size()-1)) {
                num = lista.get(i);
                return num;
            }else return Ficha_Num._1_;
        }

    }

    public static Ficha_Color fichaColorSig(Ficha_Color col) {
        ArrayList<Ficha_Color> lista = new ArrayList<>(Arrays.asList(values()));
        int i=0;
        for(Ficha_Color c: lista){
            i++;
            if(col==c){break;}
        }
        if(i< lista.size()) {
            col = lista.get(i);
            return col;
        }else return Rojo;
    }
}
