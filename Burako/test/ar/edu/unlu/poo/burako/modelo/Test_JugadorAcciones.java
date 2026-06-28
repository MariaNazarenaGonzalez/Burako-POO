package ar.edu.unlu.poo.burako.modelo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;

public class Test_JugadorAcciones {
    private Burako burako;
    private Jugador j1;

    @Before
    public void setUp() {
        burako = new Burako();
        j1 = burako.getJugador(0);
        j1.setNombre("Test Player");
    }

    @Test
    public void testBajarJuegoValido() throws Exception {
        // Vaciar atril y darle 3 fichas para un juego
        ArrayList<FichaMostrable> atrilActual = new ArrayList<>(j1.getAtril());
        for (FichaMostrable f : atrilActual) j1.sacarAtril((Ficha) f);

        j1.agregarAtril(new Ficha(Ficha_Color.Amarillo, Ficha_Num._3_));
        j1.agregarAtril(new Ficha(Ficha_Color.Amarillo, Ficha_Num._4_));
        j1.agregarAtril(new Ficha(Ficha_Color.Amarillo, Ficha_Num._5_));

        // Simulamos que el turno es correcto y ya tomó ficha
        burako.agarrarMazo(0);
        
        j1.bajarJuego(new int[]{1, 2, 3});
        Assert.assertEquals("El jugador debería tener 1 juego bajado", 1, j1.cantJuegos());
        Assert.assertEquals("Al jugador solo debería quedarle 1 ficha (la del mazo)", 1, j1.getAtril().size());
    }

    @Test
    public void testApoyarJuegoExistente() throws Exception {
        // Vaciar atril y darle 3 fichas para un juego + 1 para apoyar
        ArrayList<FichaMostrable> atrilActual = new ArrayList<>(j1.getAtril());
        for (FichaMostrable f : atrilActual) j1.sacarAtril((Ficha) f);

        j1.agregarAtril(new Ficha(Ficha_Color.Rojo, Ficha_Num._1_));
        j1.agregarAtril(new Ficha(Ficha_Color.Rojo, Ficha_Num._2_));
        j1.agregarAtril(new Ficha(Ficha_Color.Rojo, Ficha_Num._3_));
        j1.agregarAtril(new Ficha(Ficha_Color.Rojo, Ficha_Num._4_));

        j1.bajarJuego(new int[]{1, 2, 3});
        
        // El juego 1 tiene 1, 2, 3. Apoyamos el 4 al final (pos 4)
        j1.apoyarJuego(1, 4, 1);
        
        JuegoMostrable juego = j1.getJugadas().get(0);
        Assert.assertEquals("El juego debería tener 4 fichas", 4, juego.getJuego().size());
        Assert.assertTrue(j1.getAtril().isEmpty());
    }

    @Test
    public void testCalcularPuntajeCompleto() throws Exception {
        // Vaciar atril
        ArrayList<FichaMostrable> atrilActual = new ArrayList<>(j1.getAtril());
        for (FichaMostrable f : atrilActual) j1.sacarAtril((Ficha) f);

        // Bajamos una Canasta Pura (7 fichas: 3 al 9 rojos)
        ArrayList<Ficha> canasta = new ArrayList<>();
        for (int i = 3; i <= 9; i++) {
            canasta.add(new Ficha(Ficha_Color.Rojo, Ficha_Num.values()[i-1]));
        }
        j1.agregarAtril(canasta);
        j1.bajarJuego(new int[]{1, 2, 3, 4, 5, 6, 7});
        
        // Puntos de la canasta pura (7 fichas: 3-7=5pts c/u, 8-9=10pts c/u)
        // (5*5) + (2*10) = 25 + 20 = 45 pts.
        // Bono Canasta Pura = 200 pts.
        // Total bajado = 245 pts.

        // Supongamos que no tomó muerto (-100 pts) y tiene el atril vacío (0 pts)
        // Como tiene canasta, el puntaje no se vuelve negativo por la regla especial.
        // Total esperado = 245 - 100 = 145 pts.
        
        int puntos = j1.calcularPuntaje(false);
        Assert.assertEquals(145, puntos);
        
        // Si además cerró (+100) y tomó el muerto (+100 en lugar de -100)
        j1.setYaTomoMuerto(true);
        puntos = j1.calcularPuntaje(true);
        // Bajado (245) + Cerrar (100) + Muerto (100) = 445 pts.
        Assert.assertEquals(445, puntos);
    }
    
    @Test
    public void testPuntajeConCanastaImpura() throws Exception {
        // Vaciar atril
        ArrayList<FichaMostrable> atrilActual = new ArrayList<>(j1.getAtril());
        for (FichaMostrable f : atrilActual) j1.sacarAtril((Ficha) f);

        // Bajamos una Canasta Impura (6 fichas: 3 al 8 rojos + 1 comodín)
        ArrayList<Ficha> canasta = new ArrayList<>();
        for (int i = 3; i <= 8; i++) {
            canasta.add(new Ficha(Ficha_Color.Rojo, Ficha_Num.values()[i-1]));
        }
        canasta.add(new Ficha(Ficha_Color.Negro, Ficha_Num.Comodin));
        j1.agregarAtril(canasta);
        j1.bajarJuego(new int[]{1, 2, 3, 4, 5, 6, 7});
        
        // Fichas: (5*5) + (1*10) + (1*50) = 25 + 10 + 50 = 85 pts.
        // Bono Canasta Impura = 100 pts.
        // Total bajado = 185 pts.
        
        j1.setYaTomoMuerto(true);
        int puntos = j1.calcularPuntaje(false);
        // Bajado (185) + Muerto (100) = 285 pts.
        Assert.assertEquals(285, puntos);
    }
}
