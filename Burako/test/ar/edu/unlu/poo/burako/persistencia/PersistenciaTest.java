package ar.edu.unlu.poo.burako.persistencia;

import ar.edu.unlu.poo.burako.modelo.Burako;
import ar.edu.unlu.poo.burako.modelo.EstadoTurno;
import ar.edu.unlu.poo.burako.modelo.TestDataFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Tests del módulo de persistencia (PersistenciaService, repositorios,
 * Usuario, PartidaGuardada, ranking). Nueva cobertura: el proyecto no tenía
 * tests de este paquete hasta ahora.
 *
 * Cada test corre contra un directorio temporal propio (creado en @Before,
 * eliminado en @After) para no tocar la carpeta "data" real del proyecto
 * ni interferir entre tests.
 *
 * Usa TestDataFactory (paquete modelo) para construir instancias reales de
 * Burako sin depender de la interfaz gráfica.
 */
public class PersistenciaTest {

    private Path directorioTemporal;
    private PersistenciaService servicio;

    @Before
    public void setUp() throws IOException {
        directorioTemporal = Files.createTempDirectory("burako-persistencia-test-");
        servicio = new PersistenciaService(directorioTemporal.toString());
    }

    @After
    public void tearDown() throws IOException {
        try (Stream<Path> paths = Files.walk(directorioTemporal)) {
            paths.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    // ── Usuarios ─────────────────────────────────────────────────────────────

    @Test
    public void testObtenerOcrearUsuarioEsIdempotentePorNombre() {
        Usuario primero = servicio.obtenerOcrearUsuario("Ana");
        Usuario segundo = servicio.obtenerOcrearUsuario("Ana");

        Assert.assertEquals(primero.getId(), segundo.getId());
        Assert.assertEquals(1, servicio.listarUsuarios().size());
    }

    @Test
    public void testUsuariosNuevosEmpiezanConEstadisticasEnCero() {
        Usuario usuario = servicio.obtenerOcrearUsuario("Beto");
        EstadisticasUsuario stats = usuario.getEstadisticas();

        Assert.assertEquals(0, stats.getPartidasJugadas());
        Assert.assertEquals(0, stats.getVictorias());
        Assert.assertEquals(0, stats.getDerrotas());
        Assert.assertEquals(0, stats.getPuntajeAcumulado());
    }

    // ── Guardar / continuar partidas ─────────────────────────────────────────

    @Test
    public void testGuardarYContinuarPartidaPreservaElEstadoExacto() throws Exception {
        Usuario u1 = servicio.obtenerOcrearUsuario("Carla");
        Usuario u2 = servicio.obtenerOcrearUsuario("Dario");

        Burako original = TestDataFactory.crearPartidaNueva();
        TestDataFactory.tomarDelMazo(original, 0); // avanza el estado para que no sea el default

        String id = servicio.guardarPartida(null, original, u1, u2);
        Burako recuperado = servicio.continuarPartida(id);

        Assert.assertEquals(original.getTurnoActual(), recuperado.getTurnoActual());
        Assert.assertEquals(original.getEstadoTurno(), recuperado.getEstadoTurno());
        Assert.assertEquals(original.getAtril(0).size(), recuperado.getAtril(0).size());
        Assert.assertEquals(original.getNombreJugador(0), recuperado.getNombreJugador(0));
    }

    @Test
    public void testGuardarPartidaReutilizandoIdLaSobrescribe() throws Exception {
        Usuario u1 = servicio.obtenerOcrearUsuario("Carla");
        Usuario u2 = servicio.obtenerOcrearUsuario("Dario");
        Burako burako = TestDataFactory.crearPartidaNueva();

        String id = servicio.guardarPartida(null, burako, u1, u2);
        TestDataFactory.tomarDelMazo(burako, 0);
        servicio.guardarPartida(id, burako, u1, u2); // mismo id: sobrescribe

        Assert.assertEquals(1, servicio.listarPartidasGuardadas().size());
        Assert.assertEquals(EstadoTurno.JUGAR, servicio.continuarPartida(id).getEstadoTurno());
    }

    @Test
    public void testListarPartidasDeUsuarioSoloDevuelveLasQueLeCorresponden() throws Exception {
        Usuario u1 = servicio.obtenerOcrearUsuario("Carla");
        Usuario u2 = servicio.obtenerOcrearUsuario("Dario");
        Usuario u3 = servicio.obtenerOcrearUsuario("Elena");

        servicio.guardarPartida(null, TestDataFactory.crearPartidaNueva(), u1, u2);
        servicio.guardarPartida(null, TestDataFactory.crearPartidaNueva(), u1, u3);

        List<PartidaGuardada> deU2 = servicio.listarPartidasDe(u2);
        List<PartidaGuardada> deU1 = servicio.listarPartidasDe(u1);

        Assert.assertEquals(1, deU2.size());
        Assert.assertEquals(2, deU1.size());
        Assert.assertEquals(2, servicio.listarPartidasGuardadas().size());
    }

    // ── Finalización de partida y ranking ────────────────────────────────────

    @Test
    public void testFinalizarPartidaActualizaEstadisticasDeAmbosUsuarios() throws Exception {
        Usuario u1 = servicio.obtenerOcrearUsuario("Carla");
        Usuario u2 = servicio.obtenerOcrearUsuario("Dario");
        Burako finalizada = TestDataFactory.crearPartidaFinalizada();
        String id = servicio.guardarPartida(null, finalizada, u1, u2);

        servicio.finalizarPartida(finalizada, u1, u2, id);

        Usuario u1Actualizado = servicio.listarUsuarios().stream()
                .filter(u -> u.getId().equals(u1.getId())).findFirst().orElseThrow();
        Usuario u2Actualizado = servicio.listarUsuarios().stream()
                .filter(u -> u.getId().equals(u2.getId())).findFirst().orElseThrow();

        Assert.assertEquals(1, u1Actualizado.getEstadisticas().getPartidasJugadas());
        Assert.assertEquals(1, u1Actualizado.getEstadisticas().getVictorias());
        Assert.assertEquals(1, u2Actualizado.getEstadisticas().getPartidasJugadas());
        Assert.assertEquals(1, u2Actualizado.getEstadisticas().getDerrotas());
    }

    @Test
    public void testFinalizarPartidaEliminaElGuardadoIntermedio() throws Exception {
        Usuario u1 = servicio.obtenerOcrearUsuario("Carla");
        Usuario u2 = servicio.obtenerOcrearUsuario("Dario");
        Burako finalizada = TestDataFactory.crearPartidaFinalizada();
        String id = servicio.guardarPartida(null, finalizada, u1, u2);

        servicio.finalizarPartida(finalizada, u1, u2, id);

        Assert.assertTrue(servicio.listarPartidasGuardadas().isEmpty());
    }

    @Test
    public void testRankingSeOrdenaPorPuntajeAcumuladoDescendente() throws Exception {
        Usuario u1 = servicio.obtenerOcrearUsuario("Carla");
        Usuario u2 = servicio.obtenerOcrearUsuario("Dario");
        Burako finalizada = TestDataFactory.crearPartidaFinalizada();
        String id = servicio.guardarPartida(null, finalizada, u1, u2);

        servicio.finalizarPartida(finalizada, u1, u2, id);

        List<EntradaRanking> ranking = servicio.obtenerRanking();
        Assert.assertEquals(2, ranking.size());
        Assert.assertTrue("El ranking debe estar ordenado de mayor a menor puntaje",
                ranking.get(0).getPuntajeAcumulado() >= ranking.get(1).getPuntajeAcumulado());
    }
}
