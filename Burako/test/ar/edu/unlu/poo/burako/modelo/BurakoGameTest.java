package ar.edu.unlu.poo.burako.modelo;

import ar.edu.unlu.rmimvc.observer.IObservableRemoto;
import ar.edu.unlu.rmimvc.observer.IObservadorRemoto;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests de integración del flujo general de una partida, usando
 * exclusivamente la interfaz pública IBurako (el mismo contrato que ve el
 * Controlador). Reemplaza y amplía la cobertura general que antes vivía en
 * Test_FlujoJuego.
 */
public class BurakoGameTest {

    private Burako burako;

    @Before
    public void setUp() {
        burako = TestDataFactory.crearBurako();
    }

    @Test
    public void testEstadoInicial() throws Exception {
        Assert.assertEquals(12, burako.getAtril(0).size());
        Assert.assertEquals(12, burako.getAtril(1).size());
        Assert.assertEquals(0, burako.getTurnoActual());
        Assert.assertEquals(EstadoTurno.TOMAR, burako.getEstadoTurno());
    }

    @Test
    public void testSetNombres() throws Exception {
        burako.setNombres("Ana", "Beto");
        Assert.assertEquals("Ana", burako.getNombreJugador(0));
        Assert.assertEquals("Beto", burako.getNombreJugador(1));
    }

    @Test
    public void testTomarDelMazoTransicionaAJugarYCreceElAtril() throws Exception {
        boolean resultado = TestDataFactory.tomarDelMazo(burako, 0);
        Assert.assertTrue(resultado);
        Assert.assertEquals(EstadoTurno.JUGAR, burako.getEstadoTurno());
        Assert.assertEquals(13, burako.getAtril(0).size());
    }

    @Test
    public void testTomarPozoRetiraTodasLasFichasDelPozo() throws Exception {
        // j0 descarta una ficha al pozo
        TestDataFactory.tomarDelMazo(burako, 0);
        burako.agregarPozo(1, 0);

        boolean resultado = burako.agarrarPozo(1);
        Assert.assertTrue("j1 debería poder tomar del pozo", resultado);
        Assert.assertEquals(13, burako.getAtril(1).size()); // 12 + 1 del pozo
        Assert.assertTrue("El pozo debería quedar vacío tras tomar", burako.getPozo().isEmpty());
    }

    @Test
    public void testGetJugadorExponeElMismoNombreQueGetNombreJugador() throws Exception {
        burako.setNombres("Carla", "Dario");
        Assert.assertEquals("Carla", burako.getJugador(0).getNombre());
        Assert.assertEquals(burako.getNombreJugador(0), burako.getJugador(0).getNombre());
    }

    /**
     * Verifica que el mecanismo de Observer remoto (heredado de
     * ObservableRemoto) efectivamente despacha las notificaciones a los
     * observadores registrados, sin necesidad de levantar servidor/cliente
     * RMI real: se registra un IObservadorRemoto de prueba en el propio
     * proceso, tal como hace ObservadorPersistencia en producción.
     */
    @Test
    public void testObservadorEsNotificadoAlTomarDelMazo() throws Exception {
        List<Object> eventosRecibidos = new ArrayList<>();
        IObservadorRemoto espia = (observable, o) -> eventosRecibidos.add(o);

        burako.agregarObservador(espia);
        TestDataFactory.tomarDelMazo(burako, 0);

        Assert.assertEquals(1, eventosRecibidos.size());
        Assert.assertEquals(Eventos.tomarMazo_exitoso, eventosRecibidos.get(0));
    }

    @Test
    public void testObservadorRecibeEventoDeFalloCuandoLaAccionEsInvalida() throws Exception {
        List<Object> eventosRecibidos = new ArrayList<>();
        IObservadorRemoto espia = (observable, o) -> eventosRecibidos.add(o);
        burako.agregarObservador(espia);

        // j1 intenta tomar cuando es el turno de j0: debe fallar y notificar el evento _NO_exitoso.
        boolean resultado = burako.agarrarMazo(1);

        Assert.assertFalse(resultado);
        Assert.assertEquals(1, eventosRecibidos.size());
        Assert.assertEquals(Eventos.tomarMazo_NO_exitoso, eventosRecibidos.get(0));
        Assert.assertFalse(burako.getUltimoMensajeError().isEmpty());
    }
}
