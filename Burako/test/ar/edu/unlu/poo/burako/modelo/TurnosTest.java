package ar.edu.unlu.poo.burako.modelo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests centrados exclusivamente en el manejo de turnos: transiciones de
 * EstadoTurno, alternancia entre jugadores, y rechazo de acciones fuera de
 * turno o en el estado incorrecto.
 *
 * Reemplaza la porción de Test_FlujoJuego dedicada a turnos.
 */
public class TurnosTest {

    private Burako burako;

    @Before
    public void setUp() {
        burako = TestDataFactory.crearBurako();
    }

    @Test
    public void testTurnoAlternaAlDescartar() throws Exception {
        TestDataFactory.tomarDelMazo(burako, 0);
        Assert.assertEquals(EstadoTurno.JUGAR, burako.getEstadoTurno());

        burako.agregarPozo(1, 0);

        Assert.assertEquals(1, burako.getTurnoActual());
        Assert.assertEquals(EstadoTurno.TOMAR, burako.getEstadoTurno());
    }

    @Test
    public void testErrorTurnoIncorrecto() throws Exception {
        boolean resultado = burako.agarrarMazo(1);
        Assert.assertFalse("j1 no debería poder tomar ficha si es turno de j0", resultado);
        // El estado no debe haber cambiado
        Assert.assertEquals(0, burako.getTurnoActual());
        Assert.assertEquals(EstadoTurno.TOMAR, burako.getEstadoTurno());
    }

    @Test
    public void testErrorDobleToma() throws Exception {
        TestDataFactory.tomarDelMazo(burako, 0);
        boolean resultado = burako.agarrarMazo(0);
        Assert.assertFalse("j0 no debería poder tomar dos veces en el mismo turno", resultado);
    }

    @Test
    public void testErrorDescarteSinTomar() throws Exception {
        burako.agregarPozo(1, 0);
        // El turno no debe avanzar: sigue en TOMAR
        Assert.assertEquals(0, burako.getTurnoActual());
        Assert.assertEquals(EstadoTurno.TOMAR, burako.getEstadoTurno());
    }

    @Test
    public void testPuedeTomarYPuedeJugarCoherentesConElEstado() throws Exception {
        Assert.assertTrue(burako.puedeTomar(0));
        Assert.assertFalse(burako.puedeJugar(0));
        Assert.assertFalse(burako.puedeTomar(1));
        Assert.assertFalse(burako.puedeJugar(1));

        TestDataFactory.tomarDelMazo(burako, 0);

        Assert.assertFalse(burako.puedeTomar(0));
        Assert.assertTrue(burako.puedeJugar(0));
        Assert.assertFalse(burako.puedeTomar(1));
        Assert.assertFalse(burako.puedeJugar(1));
    }

    @Test
    public void testElTurnoVuelveAlJugador0TrasUnaRondaCompleta() throws Exception {
        TestDataFactory.tomarDelMazo(burako, 0);
        burako.agregarPozo(1, 0); // turno pasa a j1

        TestDataFactory.tomarDelMazo(burako, 1);
        burako.agregarPozo(1, 1); // turno vuelve a j0

        Assert.assertEquals(0, burako.getTurnoActual());
        Assert.assertEquals(EstadoTurno.TOMAR, burako.getEstadoTurno());
    }

    @Test
    public void testAgarrarPozoRespetaElMismoControlDeTurnoQueAgarrarMazo() throws Exception {
        boolean resultado = burako.agarrarPozo(1);
        Assert.assertFalse("j1 no debería poder tomar el pozo si es turno de j0", resultado);
    }
}
