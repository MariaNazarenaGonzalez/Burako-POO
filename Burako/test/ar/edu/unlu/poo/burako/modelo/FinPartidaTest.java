package ar.edu.unlu.poo.burako.modelo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Tests de las condiciones de fin de partida: toma directa e indirecta del
 * muerto, corte exitoso/fallido, y los resultados finales expuestos por
 * IBurako.getResultados().
 *
 * Reemplaza la porción de Test_Muerto.java dedicada a estos escenarios,
 * usando TestDataFactory.prepararParaCorte/crearPartidaFinalizada en lugar
 * de la preparación manual ficha por ficha.
 */
public class FinPartidaTest {

    private Burako burako;

    @Before
    public void setUp() {
        burako = TestDataFactory.crearBurako();
    }

    @Test
    public void testTomaDirectaDeMuertoAlVaciarElAtrilBajandoUnJuego() throws Exception {
        TestDataFactory.vaciarAtril(burako, 0);
        Jugador j0 = burako.getJugador(0);
        j0.agregarAtril(TestDataFactory.crearEscalera(FichaColor.Amarillo, FichaNumero.N3, 3));

        TestDataFactory.tomarDelMazo(burako, 0); // ahora tiene 4 fichas
        burako.bajarJuego(0, new int[]{1, 2, 3});

        // Queda 1 ficha (la tomada del mazo); vaciamos y bajamos un segundo juego para vaciar el atril.
        TestDataFactory.vaciarAtril(burako, 0);
        j0.agregarAtril(TestDataFactory.crearEscalera(FichaColor.Rojo, FichaNumero.N10, 3));
        burako.bajarJuego(0, new int[]{1, 2, 3});

        Assert.assertTrue("Debería haber tomado el muerto (toma directa)", j0.yaTomoMuerto());
        Assert.assertEquals(11, burako.getAtril(0).size());
        Assert.assertEquals("La toma directa no avanza el turno", EstadoTurno.JUGAR, burako.getEstadoTurno());
    }

    @Test
    public void testTomaIndirectaDeMuertoAlDescartarLaUltimaFicha() throws Exception {
        TestDataFactory.vaciarAtril(burako, 0);
        Jugador j0 = burako.getJugador(0);
        j0.agregarAtril(new Ficha(FichaColor.Amarillo, FichaNumero.N1));

        TestDataFactory.tomarDelMazo(burako, 0); // 2 fichas
        j0.sacarAtril((Ficha) j0.getAtril().get(0)); // dejamos exactamente 1

        burako.agregarPozo(1, 0); // descarta la última -> toma indirecta

        Assert.assertTrue("Debería haber tomado el muerto (toma indirecta)", j0.yaTomoMuerto());
        Assert.assertEquals(11, burako.getAtril(0).size());
        Assert.assertEquals("La toma indirecta SÍ avanza el turno", 1, burako.getTurnoActual());
        Assert.assertEquals(EstadoTurno.TOMAR, burako.getEstadoTurno());
    }

    @Test
    public void testErrorAlIntentarCortarSinCanasta() throws Exception {
        TestDataFactory.vaciarAtril(burako, 0);
        Jugador j0 = burako.getJugador(0);
        j0.setYaTomoMuerto(true);
        j0.agregarAtril(new Ficha(FichaColor.Amarillo, FichaNumero.N1));

        TestDataFactory.tomarDelMazo(burako, 0); // 2 fichas
        burako.agregarPozo(1, 0); // descarta 1, queda 1

        burako.agregarPozo(1, 0); // intenta descartar la última sin canasta

        Assert.assertNotEquals("La partida no debería terminar sin canasta",
                EstadoTurno.PARTIDA_TERMINADA, burako.getEstadoTurno());
        Assert.assertFalse(burako.getUltimoMensajeError().isEmpty());
    }

    @Test
    public void testCorteExitosoConCanastaYMuertoTomado() throws Exception {
        TestDataFactory.prepararParaCorte(burako, 0, FichaColor.Rojo, FichaNumero.N3);

        burako.agregarPozo(1, 0); // descarta la última ficha -> corte

        Assert.assertEquals(EstadoTurno.PARTIDA_TERMINADA, burako.getEstadoTurno());
    }

    @Test
    public void testPartidaFinalizadaFactoryProduceResultadosCoherentes() throws Exception {
        Burako finalizada = TestDataFactory.crearPartidaFinalizada();

        Assert.assertEquals(EstadoTurno.PARTIDA_TERMINADA, finalizada.getEstadoTurno());

        List<ResultadoJugador> resultados = finalizada.getResultados();
        Assert.assertEquals(2, resultados.size());
        Assert.assertTrue("El jugador 0 cortó y debería figurar como ganador",
                resultados.get(0).esGanador());
        Assert.assertFalse(resultados.get(1).esGanador());
    }

    @Test
    public void testHayQuiebrePorMazoVacio() {
        ContextoJugada ctxConMazo = ContextoJugada.builder().mazoVacio(false).build();
        ContextoJugada ctxSinMazo = ContextoJugada.builder().mazoVacio(true).build();

        Assert.assertFalse(ReglasDeJuego.hayQuiebrePorMazoVacio(ctxConMazo));
        Assert.assertTrue(ReglasDeJuego.hayQuiebrePorMazoVacio(ctxSinMazo));
    }
}
