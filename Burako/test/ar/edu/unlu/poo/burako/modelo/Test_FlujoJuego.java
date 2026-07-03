package ar.edu.unlu.poo.burako.modelo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Test_FlujoJuego {
    private Burako burako;

    @Before
    public void setUp() {
        burako = new Burako();
    }

    @Test
    public void testEstadoInicial() throws Exception {
        // En Burako individual (2 jugadores), cada uno tiene 12 fichas iniciales.
        Assert.assertEquals(12, burako.getJugador(0).getAtril().size());
        Assert.assertEquals(12, burako.getJugador(1).getAtril().size());
        Assert.assertEquals(0, burako.getTurnoActual());
        Assert.assertEquals(EstadoTurno.TOMAR, burako.getEstadoTurno());
    }

    @Test
    public void testTurnoAlternancia() throws Exception {
        // Jugador 0 toma y descarta
        burako.agarrarMazo(0);
        Assert.assertEquals(EstadoTurno.JUGAR, burako.getEstadoTurno());
        burako.agregarPozo(1, 0);
        
        // Debe ser turno del jugador 1
        Assert.assertEquals(1, burako.getTurnoActual());
        Assert.assertEquals(EstadoTurno.TOMAR, burako.getEstadoTurno());
    }

    @Test
    public void testErrorTurnoIncorrecto() throws Exception {
        // j1 intenta tomar del mazo cuando es el turno de j0
        boolean resultado = burako.agarrarMazo(1);
        Assert.assertFalse("j1 no debería poder tomar ficha si es turno de j0", resultado);
    }

    @Test
    public void testErrorDobleToma() throws Exception {
        burako.agarrarMazo(0);
        // j0 intenta tomar de nuevo en el mismo turno
        boolean resultado = burako.agarrarMazo(0);
        Assert.assertFalse("j0 no debería poder tomar dos veces ficha del mazo/pozo", resultado);
    }

    @Test
    public void testErrorDescarteSinTomar() throws Exception {
        // j0 intenta descartar sin haber tomado ficha del mazo
        burako.agregarPozo(1, 0);
        // El turno no debe avanzar
        Assert.assertEquals(0, burako.getTurnoActual());
        Assert.assertEquals(EstadoTurno.TOMAR, burako.getEstadoTurno());
    }

    @Test
    public void testTomarPozo() throws Exception {
        // Primero j0 descarta una ficha al pozo
        burako.agarrarMazo(0);
        burako.agregarPozo(1, 0);
        
        // Ahora j1 toma del pozo
        boolean resultado = burako.agarrarPozo(1);
        Assert.assertTrue("j1 debería poder tomar del pozo", resultado);
        Assert.assertEquals(13, burako.getJugador(1).getAtril().size()); // 12 + 1 del pozo
        Assert.assertTrue("El pozo debería estar vacío tras tomar", burako.getPozo().isEmpty());
    }
}
