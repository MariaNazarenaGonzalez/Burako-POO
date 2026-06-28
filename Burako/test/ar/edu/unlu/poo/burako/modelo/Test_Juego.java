package ar.edu.unlu.poo.burako.modelo;

import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;

public class Test_Juego {

    // --- ESCALERAS ---

    @Test
    public void testCrearEscaleraValida() throws Exception {
        ArrayList<Ficha> fichas = new ArrayList<>();
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._3_));
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._4_));
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._5_));

        Juego j = new Juego(fichas);
        Assert.assertEquals(Juego_tipo.Escalera, j.getTipo());
    }

    @Test(expected = Exception.class)
    public void testErrorEscaleraColoresMezclados() throws Exception {
        ArrayList<Ficha> fichas = new ArrayList<>();
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._3_));
        fichas.add(new Ficha(Ficha_Color.Rojo, Ficha_Num._4_)); // Color distinto
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._5_));
        new Juego(fichas);
    }

    @Test(expected = Exception.class)
    public void testErrorEscaleraNumerosNoSucesivos() throws Exception {
        ArrayList<Ficha> fichas = new ArrayList<>();
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._3_));
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._5_)); // Salto
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._6_));
        new Juego(fichas);
    }

    // --- PIERNAS ---

    @Test
    public void testCrearPiernaValida() throws Exception {
        ArrayList<Ficha> fichas = new ArrayList<>();
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._1_));
        fichas.add(new Ficha(Ficha_Color.Rojo, Ficha_Num._1_));
        fichas.add(new Ficha(Ficha_Color.Negro, Ficha_Num._1_));

        Juego j = new Juego(fichas);
        Assert.assertEquals(Juego_tipo.Pierna, j.getTipo());
    }

    @Test(expected = Exception.class)
    public void testErrorPiernaNumerosMezclados() throws Exception {
        ArrayList<Ficha> fichas = new ArrayList<>();
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._3_));
        fichas.add(new Ficha(Ficha_Color.Rojo, Ficha_Num._4_)); // No es comodín ni igual al anterior
        fichas.add(new Ficha(Ficha_Color.Negro, Ficha_Num._3_));
        new Juego(fichas);
    }

    // --- COMODINES ---

    @Test
    public void testEscaleraConComodinInterno() throws Exception {
        ArrayList<Ficha> fichas = new ArrayList<>();
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._3_));
        fichas.add(new Ficha(Ficha_Color.Negro, Ficha_Num.Comodin)); // Reemplaza al 4
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._5_));

        Juego j = new Juego(fichas);
        Assert.assertEquals(Juego_tipo.Escalera, j.getTipo());
    }

    @Test
    public void testEscaleraConDosComoComodin() throws Exception {
        ArrayList<Ficha> fichas = new ArrayList<>();
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._8_));
        fichas.add(new Ficha(Ficha_Color.Azul, Ficha_Num._2_)); // El 2 azul como comodín del 9 amarillo
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._10_));

        Juego j = new Juego(fichas);
        Assert.assertEquals(Juego_tipo.Escalera, j.getTipo());
    }

    // --- CANASTAS ---

    @Test
    public void testTransicionACanastaPura() throws Exception {
        ArrayList<Ficha> fichas = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            fichas.add(new Ficha(Ficha_Color.Rojo, Ficha_Num.values()[i-1]));
        }
        Juego j = new Juego(fichas);
        Assert.assertEquals(Juego_tipo.Escalera, j.getTipo());

        // Agregamos la 7ma ficha
        j.agregar(new Ficha(Ficha_Color.Rojo, Ficha_Num._7_), 7);
        Assert.assertEquals(Juego_tipo.Canasta_pura_escalera, j.getTipo());
        Assert.assertEquals("El puntaje de canasta pura (3-7=5pts*5 + 1-2=15+20=35) + 200 bono", 260, j.getPuntaje());
    }

    @Test
    public void testCanastaImpuraPorComodin() throws Exception {
        ArrayList<Ficha> fichas = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            fichas.add(new Ficha(Ficha_Color.Rojo, Ficha_Num.values()[i-1]));
        }
        fichas.add(new Ficha(Ficha_Color.Negro, Ficha_Num.Comodin));

        Juego j = new Juego(fichas);
        Assert.assertEquals(Juego_tipo.Canasta_inpura_escalera, j.getTipo());
    }

    // --- AGREGAR EN PRIMERA POSICIÓN ---

    @Test
    public void testAgregarAlInicioEscalera() throws Exception {
        ArrayList<Ficha> fichas = new ArrayList<>();
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._4_));
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._5_));
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._6_));

        Juego j = new Juego(fichas);
        // Agregamos el 3 amarillo en la posición 1
        j.agregar(new Ficha(Ficha_Color.Amarillo, Ficha_Num._3_), 1);

        Assert.assertEquals(4, j.getJuego().size());
        Assert.assertEquals(Ficha_Num._3_, j.getJuego().get(0).getNum());
    }

    @Test
    public void testAgregarComodinAlInicioEscalera() throws Exception {
        ArrayList<Ficha> fichas = new ArrayList<>();
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._4_));
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._5_));
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._6_));

        Juego j = new Juego(fichas);
        // Agregamos un comodín en la posición 1 (actuaría como el 3)
        j.agregar(new Ficha(Ficha_Color.Negro, Ficha_Num.Comodin), 1);

        Assert.assertEquals(4, j.getJuego().size());
        Assert.assertEquals(Ficha_Num.Comodin, j.getJuego().get(0).getNum());
    }

    @Test
    public void testReemplazarComodinAlInicio() throws Exception {
        ArrayList<Ficha> fichas = new ArrayList<>();
        fichas.add(new Ficha(Ficha_Color.Negro, Ficha_Num.Comodin)); // Representa al 3
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._4_));
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._5_));

        Juego j = new Juego(fichas);
        // Al agregar el 3 real en la pos 1, el comodín debe desplazarse al final (pos 4)
        j.agregar(new Ficha(Ficha_Color.Amarillo, Ficha_Num._3_), 1);

        Assert.assertEquals(4, j.getJuego().size());
        Assert.assertEquals(Ficha_Num._3_, j.getJuego().get(0).getNum());
        Assert.assertEquals(Ficha_Num.Comodin, j.getJuego().get(3).getNum()); // Desplazado
    }

    @Test
    public void testEscaleraCircularKantesDeA() throws Exception {
        ArrayList<Ficha> fichas = new ArrayList<>();
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._1_)); // As
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._2_));
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._3_));

        Juego j = new Juego(fichas);
        // En Burako, el 13 (Rey) puede ir antes del 1 (As)
        j.agregar(new Ficha(Ficha_Color.Amarillo, Ficha_Num._13_), 1);

        Assert.assertEquals(4, j.getJuego().size());
        Assert.assertEquals(Ficha_Num._13_, j.getJuego().get(0).getNum());
    }

    // --- REGLAS ESPECIALES DE COMODÍN (AGREGAR/REEMPLAZAR) ---

    @Test
    public void testReemplazarComodinPorFichaReal() throws Exception {
        ArrayList<Ficha> fichas = new ArrayList<>();
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._4_));
        fichas.add(new Ficha(Ficha_Color.Negro, Ficha_Num.Comodin)); // Está en lugar del 5
        fichas.add(new Ficha(Ficha_Color.Amarillo, Ficha_Num._6_));

        Juego j = new Juego(fichas);
        
        // Al agregar el 5 amarillo en la posición 2 (donde está el comodín)
        // La lógica del modelo debería mover el comodín al final (según reglas y código previo)
        j.agregar(new Ficha(Ficha_Color.Amarillo, Ficha_Num._5_), 2);
        
        Assert.assertEquals(4, j.getJuego().size());
        // El comodín debería haberse movido (ahora es el 4to elemento)
        Assert.assertEquals(Ficha_Num.Comodin, j.getJuego().get(3).getNum());
        Assert.assertEquals(Ficha_Num._5_, j.getJuego().get(1).getNum());
    }

    @Test
    public void testPuntajeCanastaPiernaPura() throws Exception {
        ArrayList<Ficha> fichas = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            fichas.add(new Ficha(Ficha_Color.values()[i%4], Ficha_Num._1_)); // Siete "1" de varios colores
        }
        Juego j = new Juego(fichas);
        Assert.assertEquals(Juego_tipo.Canasta_pura_pierna, j.getTipo());
        // Cada "1" vale 15 pts. 15 * 7 = 105. Bono = 200. Total = 305.
        Assert.assertEquals(305, j.getPuntaje());
    }
}
