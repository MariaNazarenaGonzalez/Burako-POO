package ar.edu.unlu.poo.burako.modelo;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Tests de la lógica de 4 jugadores agrupados en 2 equipos de 2
 * (jugadores 0 y 2 forman un equipo; 1 y 3 forman el otro). Reglas
 * cubiertas:
 *  - Los compañeros comparten ÚNICAMENTE el muerto (un muerto por equipo,
 *    no por jugador); el resto (atril, juegos bajados, puntaje) es
 *    individual.
 *  - Si un integrante toma el muerto de su equipo, su compañero queda
 *    marcado como "ya tomó muerto" a los efectos de puntaje, y ninguno de
 *    los dos puede tomar un segundo muerto.
 *  - getResultados() marca como ganadores a AMBOS integrantes del equipo
 *    del jugador que cortó, aunque el corte en sí (bono +100) siga siendo
 *    individual de quien lo ejecuta.
 *  - Con 2 jugadores, el comportamiento es exactamente el de fases
 *    anteriores (cada jugador es su propio equipo).
 */
public class EquiposTest {

    @Test
    public void testPartidaDeCuatroRepartaOnceFichasPorJugador() throws Exception {
        Burako burako = TestDataFactory.crearBurako(4);
        Assert.assertEquals(4, burako.getCantidadJugadores());
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(11, burako.getAtril(i).size());
        }
    }

    @Test
    public void testConstructorRechazaCantidadesInvalidas() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new Burako(3));
        Assert.assertThrows(IllegalArgumentException.class, () -> new Burako(1));
        Assert.assertThrows(IllegalArgumentException.class, () -> new Burako(5));
    }

    @Test
    public void testEquipoDeAgrupaJugadores0y2Y1y3() throws Exception {
        Burako burako = TestDataFactory.crearBurako(4);
        Assert.assertEquals(burako.getEquipo(0), burako.getEquipo(2));
        Assert.assertEquals(burako.getEquipo(1), burako.getEquipo(3));
        Assert.assertNotEquals(burako.getEquipo(0), burako.getEquipo(1));
    }

    @Test
    public void testCon2JugadoresCadaUnoEsSuPropioEquipo() throws Exception {
        Burako burako = TestDataFactory.crearBurako();
        Assert.assertNotEquals(burako.getEquipo(0), burako.getEquipo(1));
    }

    @Test
    public void testSetNombresConListaAsignaEnOrden() throws Exception {
        Burako burako = TestDataFactory.crearBurako(4);
        burako.setNombres(List.of("Ana", "Beto", "Carla", "Dario"));

        Assert.assertEquals("Ana", burako.getNombreJugador(0));
        Assert.assertEquals("Beto", burako.getNombreJugador(1));
        Assert.assertEquals("Carla", burako.getNombreJugador(2));
        Assert.assertEquals("Dario", burako.getNombreJugador(3));
    }

    /**
     * Si el jugador 0 toma el muerto (toma directa), su compañero de
     * equipo (jugador 2) debe quedar también marcado como "ya tomó
     * muerto" para el puntaje, aunque NO reciba las 11 fichas físicas del
     * muerto (esas solo las recibe quien lo tomó). Los jugadores 1 y 3
     * (el otro equipo) no deben verse afectados.
     */
    @Test
    public void testTomaDeMuertoSeComparteSoloDentroDelEquipo() throws Exception {
        Burako burako = TestDataFactory.crearBurako(4);

        // Preparamos al jugador 0 para una toma directa: baja una escalera de
        // 3 fichas y le queda el atril vacío (recibió 11, deja 3 para bajar,
        // toma 1 del mazo primero para pasar de TOMAR a JUGAR).
        TestDataFactory.tomarDelMazo(burako, 0);
        TestDataFactory.vaciarAtril(burako, 0);
        Jugador j0 = burako.getJugador(0);
        j0.agregarAtril(TestDataFactory.crearEscalera(FichaColor.Amarillo, FichaNumero.N3, 3));
        burako.bajarJuego(0, new int[]{1, 2, 3});

        Assert.assertTrue("j0 debería haber tomado el muerto de su equipo", j0.yaTomoMuerto());
        Assert.assertEquals("j0 debe tener las 11 fichas físicas del muerto", 11, burako.getAtril(0).size());

        Jugador j2 = burako.getJugador(2);
        Assert.assertTrue("j2 (compañero de j0) debe quedar marcado como yaTomoMuerto",
                j2.yaTomoMuerto());
        Assert.assertEquals("j2 NO debe recibir fichas físicas: no fue quien tomó el muerto",
                11, burako.getAtril(2).size()); // su mano original de 11, sin cambios

        Jugador j1 = burako.getJugador(1);
        Jugador j3 = burako.getJugador(3);
        Assert.assertFalse("j1 (otro equipo) no debe verse afectado", j1.yaTomoMuerto());
        Assert.assertFalse("j3 (otro equipo) no debe verse afectado", j3.yaTomoMuerto());
    }

    /**
     * Una vez que el equipo de j0 (j0 y j2) ya tomó su único muerto,
     * ninguno de los dos integrantes debe poder tomar un segundo muerto.
     * Para que sea el turno legítimo de j2 se hace avanzar el turno
     * realmente (j0 descarta, j1 toma y descarta), en vez de invocar
     * bajarJuego(2, ...) fuera de turno.
     */
    @Test
    public void testUnEquipoNoPuedeTomarUnSegundoMuerto() throws Exception {
        Burako burako = TestDataFactory.crearBurako(4);

        // Turno de j0: toma el muerto de su equipo (toma directa) y descarta
        // una ficha del muerto para cederle el turno a j1.
        TestDataFactory.tomarDelMazo(burako, 0);
        TestDataFactory.vaciarAtril(burako, 0);
        Jugador j0 = burako.getJugador(0);
        j0.agregarAtril(TestDataFactory.crearEscalera(FichaColor.Amarillo, FichaNumero.N3, 3));
        burako.bajarJuego(0, new int[]{1, 2, 3});
        Assert.assertTrue(j0.yaTomoMuerto());
        burako.agregarPozo(1, 0); // descarta 1 de las 11 fichas del muerto -> turno pasa a j1

        // Turno de j1: no hace nada relevante, solo cede el turno a j2.
        Assert.assertEquals(1, burako.getTurnoActual());
        TestDataFactory.tomarDelMazo(burako, 1);
        burako.agregarPozo(1, 1); // turno pasa a j2

        // Turno de j2 (compañero de j0): intenta la misma jugada. Como su
        // equipo ya no tiene muerto disponible y ya está marcado
        // yaTomoMuerto, NO debería recibir un segundo muerto.
        Assert.assertEquals(2, burako.getTurnoActual());
        TestDataFactory.tomarDelMazo(burako, 2);
        TestDataFactory.vaciarAtril(burako, 2);
        Jugador j2 = burako.getJugador(2);
        j2.agregarAtril(TestDataFactory.crearEscalera(FichaColor.Rojo, FichaNumero.N3, 3));
        burako.bajarJuego(2, new int[]{1, 2, 3});

        Assert.assertEquals("j2 no debe recibir un segundo muerto: su equipo ya no tiene",
                0, burako.getAtril(2).size());

        // Verificamos que el otro equipo (j1/j3) sigue con su muerto disponible.
        Assert.assertFalse("j1 (otro equipo) todavía no tomó muerto", j1YaTomoMuerto(burako));
    }

    private boolean j1YaTomoMuerto(Burako burako) throws Exception {
        return burako.getJugador(1).yaTomoMuerto();
    }

    /**
     * Al finalizar la partida, ambos integrantes del equipo que cortó
     * deben figurar como ganadores en getResultados(), aunque el bono de
     * +100 por cortar siga siendo individual de quien ejecutó el corte.
     */
    @Test
    public void testGetResultadosMarcaGanadorATodoElEquipo() throws Exception {
        Burako burako = TestDataFactory.crearBurako(4);
        burako.setNombres(List.of("Ana", "Beto", "Carla", "Dario"));

        // Preparamos a j0 (equipo 0, junto a j2) para cortar: canasta bajada,
        // muerto tomado, y una única ficha restante en el atril.
        TestDataFactory.tomarDelMazo(burako, 0);
        TestDataFactory.vaciarAtril(burako, 0);
        Jugador j0 = burako.getJugador(0);
        j0.agregarAtril(TestDataFactory.crearCanastaLimpia(FichaColor.Rojo, FichaNumero.N3));
        j0.bajarJuego(new int[]{1, 2, 3, 4, 5, 6, 7});
        j0.setYaTomoMuerto(true);
        j0.agregarAtril(new Ficha(FichaColor.Azul, FichaNumero.N9));

        burako.agregarPozo(1, 0); // descarta la última ficha -> corte

        Assert.assertEquals(EstadoTurno.PARTIDA_TERMINADA, burako.getEstadoTurno());

        List<ResultadoJugador> resultados = burako.getResultados();
        Assert.assertEquals(4, resultados.size());
        Assert.assertTrue("j0 cortó: debe ser ganador", resultados.get(0).esGanador());
        Assert.assertTrue("j2 es compañero de j0: también debe ser ganador", resultados.get(2).esGanador());
        Assert.assertFalse("j1 es del otro equipo: no debe ser ganador", resultados.get(1).esGanador());
        Assert.assertFalse("j3 es del otro equipo: no debe ser ganador", resultados.get(3).esGanador());
    }
}
