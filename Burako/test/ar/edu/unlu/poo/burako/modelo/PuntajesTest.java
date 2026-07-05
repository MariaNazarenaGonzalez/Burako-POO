package ar.edu.unlu.poo.burako.modelo;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Tests de las reglas de puntaje: valor individual de cada ficha, bonos de
 * canasta limpia/sucia, y el cálculo final del puntaje de un jugador
 * (ReglasDeJuego.calcularPuntaje / Jugador.calcularPuntaje).
 *
 * Reemplaza la porción de puntajes de Test_Componentes, Test_Muerto y
 * Test_JugadorAcciones, usando TestDataFactory en lugar de vaciar el atril
 * manualmente ficha por ficha.
 */
public class PuntajesTest {

    // ── Valor individual de cada ficha ──────────────────────────────────────

    @Test
    public void testValorPorFicha() {
        Assert.assertEquals(15, new Ficha(FichaColor.Rojo, FichaNumero.N1).getValor());
        Assert.assertEquals(20, new Ficha(FichaColor.Azul, FichaNumero.N2).getValor());
        Assert.assertEquals(5, new Ficha(FichaColor.Amarillo, FichaNumero.N5).getValor());
        Assert.assertEquals(10, new Ficha(FichaColor.Negro, FichaNumero.N10).getValor());
        Assert.assertEquals(50, new Ficha(FichaColor.Negro, FichaNumero.Comodin).getValor());
    }

    // ── Bono de canasta limpia vs. sucia (Juego.calcularPuntaje) ────────────

    @Test
    public void testBonoDeCanastaLimpiaEsDoscientos() throws Exception {
        // 3,4,5,6,7,8,9 rojos: (5*5)+(2*10) = 45 pts + 200 de bono = 245.
        Juego j = new Juego(TestDataFactory.crearCanastaLimpia(FichaColor.Rojo, FichaNumero.N3));
        Assert.assertEquals(245, j.calcularPuntaje());
    }

    @Test
    public void testBonoDeCanastaSuciaEsCien() throws Exception {
        // 3,4,5,6,7,8 rojos + comodín: (5*5)+(1*10)+(1*50) = 85 pts + 100 de bono = 185.
        Juego j = new Juego(TestDataFactory.crearCanastaSucia(FichaColor.Rojo, FichaNumero.N3));
        Assert.assertEquals(185, j.calcularPuntaje());
    }

    // ── Puntaje final del jugador (Jugador.calcularPuntaje) ─────────────────

    @Test
    public void testPuntajeSinCanastaEsSiempreNegativo() throws Exception {
        Jugador jugador = TestDataFactory.crearJugadorSinCartas();
        // 10,11,12 azules (10 pts c/u = 30) bajados sin formar canasta (menos de 7 fichas).
        jugador.agregarAtril(TestDataFactory.crearEscalera(FichaColor.Azul, FichaNumero.N10, 3));
        jugador.bajarJuego(new int[]{1, 2, 3});

        int puntos = jugador.calcularPuntaje(false);
        Assert.assertTrue("El puntaje debería ser negativo por no tener canasta", puntos < 0);
    }

    @Test
    public void testPuntajeConCanastaPuraYBonosDeCorteYMuerto() throws Exception {
        Jugador jugador = TestDataFactory.crearJugadorConCanasta(); // canasta limpia 3-9 rojos = 245 pts

        // Sin cortar, sin tomar muerto: 245 - 100 (penalización por no tomar muerto) = 145.
        Assert.assertEquals(145, jugador.calcularPuntaje(false));

        // Si además cortó y tomó el muerto: 245 + 100 (corte) + 100 (muerto) = 445.
        jugador.setYaTomoMuerto(true);
        Assert.assertEquals(445, jugador.calcularPuntaje(true));
    }

    @Test
    public void testPuntajeConCanastaImpura() throws Exception {
        Jugador jugador = TestDataFactory.crearJugadorSinCartas();
        jugador.agregarAtril(TestDataFactory.crearCanastaSucia(FichaColor.Rojo, FichaNumero.N3)); // 185 pts
        jugador.bajarJuego(new int[]{1, 2, 3, 4, 5, 6, 7});
        jugador.setYaTomoMuerto(true);

        // 185 + 100 (tomó muerto) = 285.
        Assert.assertEquals(285, jugador.calcularPuntaje(false));
    }

    // ── ReglasDeJuego.calcularPuntaje invocado directamente ─────────────────

    @Test
    public void testReglasDeJuegoCalcularPuntajeConCanastaYSinBonos() throws Exception {
        List<Juego> juegos = List.of(new Juego(TestDataFactory.crearCanastaLimpia(FichaColor.Rojo, FichaNumero.N3)));
        List<Ficha> atrilRestante = List.of(); // atril vacío

        int puntos = ReglasDeJuego.calcularPuntaje(juegos, atrilRestante, true, false, false);
        // 245 (canasta limpia) - 0 (atril vacío) - 100 (no tomó muerto) = 145.
        Assert.assertEquals(145, puntos);
    }

    @Test
    public void testReglasDeJuegoCalcularPuntajeSinCanastaRestaTodo() throws Exception {
        List<Juego> juegos = List.of(); // no bajó nada
        List<Ficha> atrilRestante = TestDataFactory.crearEscalera(FichaColor.Azul, FichaNumero.N10, 3); // 30 pts

        int puntos = ReglasDeJuego.calcularPuntaje(juegos, atrilRestante, false, false, false);
        // -(0 + 30) - 100 (no tomó muerto) = -130.
        Assert.assertEquals(-130, puntos);
    }
}
