package ar.edu.unlu.poo.burako.modelo;

import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;

public class Test_Componentes {

    @Test
    public void testMazoCompleto() {
        Mazo mazo = new Mazo();
        ArrayList<Ficha> fichas = mazo.sacar(106);
        Assert.assertEquals("El mazo debe tener 106 fichas", 106, fichas.size());
        
        int comodines = 0;
        for (Ficha f : fichas) {
            if (f.getNum() == Ficha_Num.Comodin) comodines++;
        }
        Assert.assertEquals("El mazo debe tener 2 comodines", 2, comodines);
    }

    @Test
    public void testPozoFuncionalidad() throws Exception {
        Pozo pozo = new Pozo();
        Ficha f1 = new Ficha(Ficha_Color.Rojo, Ficha_Num._1_);
        Ficha f2 = new Ficha(Ficha_Color.Azul, Ficha_Num._2_);
        
        pozo.agregar(f1);
        pozo.agregar(f2);
        
        ArrayList<FichaMostrable> mostrable = pozo.get();
        Assert.assertEquals(2, mostrable.size());
        
        ArrayList<Ficha> tomadas = pozo.tomar();
        Assert.assertEquals(2, tomadas.size());
        Assert.assertTrue(pozo.get().isEmpty());
    }

    @Test
    public void testAtrilFuncionalidad() throws Exception {
        ArrayList<Ficha> inicial = new ArrayList<>();
        Ficha f1 = new Ficha(Ficha_Color.Negro, Ficha_Num._10_);
        inicial.add(f1);
        Atril atril = new Atril(inicial);
        
        Assert.assertEquals(1, atril.get().size());
        
        // Ver ficha (1-based index)
        ArrayList<Ficha> vistas = atril.ver(new int[]{1});
        Assert.assertEquals(f1, vistas.get(0));
        
        // Agregar
        Ficha f2 = new Ficha(Ficha_Color.Amarillo, Ficha_Num._5_);
        atril.agregar(f2);
        Assert.assertEquals(2, atril.get().size());
        
        // Sacar
        ArrayList<Ficha> aSacar = new ArrayList<>();
        aSacar.add(f1);
        atril.sacar(aSacar);
        Assert.assertEquals(1, atril.get().size());
        Assert.assertEquals(f2, ((Ficha)atril.get().get(0)));
    }

    @Test
    public void testFichaValores() {
        Assert.assertEquals(15, new Ficha(Ficha_Color.Rojo, Ficha_Num._1_).getValor());
        Assert.assertEquals(20, new Ficha(Ficha_Color.Azul, Ficha_Num._2_).getValor());
        Assert.assertEquals(5, new Ficha(Ficha_Color.Amarillo, Ficha_Num._5_).getValor());
        Assert.assertEquals(10, new Ficha(Ficha_Color.Negro, Ficha_Num._10_).getValor());
        Assert.assertEquals(50, new Ficha(Ficha_Color.Negro, Ficha_Num.Comodin).getValor());
    }
}
