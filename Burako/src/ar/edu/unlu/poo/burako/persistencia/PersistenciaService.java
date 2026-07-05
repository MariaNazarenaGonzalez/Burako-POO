package ar.edu.unlu.poo.burako.persistencia;

import ar.edu.unlu.poo.burako.modelo.Burako;
import ar.edu.unlu.poo.burako.modelo.ResultadoJugador;

import java.io.File;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

/**
 * Fachada única de acceso a la persistencia (equivalente al
 * "PersistenciaService" sugerido en la consigna).
 *
 * Coordina {@link RepositorioUsuarios}, {@link RepositorioRanking} y
 * {@link RepositorioPartidas} sin exponer sus detalles internos a quien
 * la usa (Main, o un futuro Observador/Controlador). Es el único punto del
 * proyecto que conoce una ruta base de archivos concreta: el modelo
 * (Burako y colaboradores) permanece totalmente ignorante del sistema de
 * archivos, tal como exige la consigna.
 */
public class PersistenciaService {

    private final IRepositorioUsuarios repositorioUsuarios;
    private final IRepositorioRanking  repositorioRanking;
    private final IRepositorioPartidas repositorioPartidas;

    /** @param directorioBase carpeta raíz donde se guardarán usuarios.dat, ranking.dat y partidas/. */
    public PersistenciaService(String directorioBase) {
        File base = new File(directorioBase);
        this.repositorioUsuarios = new RepositorioUsuarios(new File(base, "usuarios.dat"));
        this.repositorioRanking  = new RepositorioRanking(new File(base, "ranking.dat"));
        this.repositorioPartidas = new RepositorioPartidas(new File(base, "partidas"));
    }

    /** Constructor para inyectar repositorios alternativos (tests, mocks). */
    public PersistenciaService(IRepositorioUsuarios repositorioUsuarios,
                                IRepositorioRanking repositorioRanking,
                                IRepositorioPartidas repositorioPartidas) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.repositorioRanking  = repositorioRanking;
        this.repositorioPartidas = repositorioPartidas;
    }

    // ── Usuarios ─────────────────────────────────────────────────────────────

    /** Retorna el usuario existente con ese nombre, o lo registra si todavía no existe. */
    public Usuario obtenerOcrearUsuario(String nombre) {
        return repositorioUsuarios.buscarPorNombre(nombre)
                .orElseGet(() -> repositorioUsuarios.registrar(nombre));
    }

    public List<Usuario> listarUsuarios() {
        return repositorioUsuarios.listarTodos();
    }

    // ── Partidas ─────────────────────────────────────────────────────────────

    /**
     * Guarda (o sobrescribe) el estado completo de una partida en curso.
     * @param idPartida id a reutilizar (partida ya guardada antes), o null para generar uno nuevo.
     * @return el id bajo el cual quedó guardada la partida.
     */
    public String guardarPartida(String idPartida, Burako estado, Usuario usuario1, Usuario usuario2) {
        return guardarPartida(idPartida, estado, List.of(usuario1, usuario2));
    }

    /**
     * Guarda (o sobrescribe) el estado completo de una partida de 2 o 4
     * usuarios. NUEVO (Fase 10 - Soporte 2 o 4 jugadores).
     */
    public String guardarPartida(String idPartida, Burako estado, List<Usuario> usuarios) {
        String id = idPartida != null ? idPartida : UUID.randomUUID().toString();
        repositorioPartidas.guardar(new PartidaGuardada(id, usuarios, estado));
        return id;
    }

    /**
     * Recupera el estado exacto de una partida guardada (turno, manos, mazo,
     * descarte, combinaciones bajadas y muertos), lista para continuar
     * normalmente desde donde quedó.
     */
    public Burako continuarPartida(String idPartida) {
        return repositorioPartidas.buscarPorId(idPartida)
                .orElseThrow(() -> new PersistenciaException("No existe una partida guardada con id " + idPartida))
                .getEstado();
    }

    /** Lista las partidas guardadas en las que participa el usuario dado. */
    public List<PartidaGuardada> listarPartidasDe(Usuario usuario) {
        return repositorioPartidas.listarPorUsuario(usuario.getId());
    }

    /**
     * Lista todas las partidas guardadas existentes, sin filtrar por usuario.
     * Método de consulta agregado para la opción "Cargar partida" del menú
     * gráfico: permite ofrecer todas las partidas disponibles sin exigir
     * que el usuario se identifique primero.
     */
    public List<PartidaGuardada> listarPartidasGuardadas() {
        return repositorioPartidas.listarTodas();
    }

    // ── Cierre de partida y ranking ─────────────────────────────────────────

    /**
     * Aplica los resultados finales de una partida terminada (ver
     * {@code Eventos.partida_terminada}) a las estadísticas de ambos
     * usuarios y al ranking, y elimina el guardado intermedio de la
     * partida, ya que dejó de estar "en curso".
     *
     * MODIFICADO (Fase 9 - RMIMVC): declara "throws RemoteException" porque
     * internamente llama a estado.getResultados(), método de IBurako que
     * ahora puede lanzar esa excepción. Esta clase se sigue ejecutando
     * exclusivamente en el servidor (la llama ObservadorPersistencia,
     * registrado únicamente allí), por lo que no hay ningún cambio de
     * responsabilidad: sigue siendo persistencia server-side pura.
     */
    public void finalizarPartida(Burako estado, Usuario usuario1, Usuario usuario2, String idPartidaGuardada) throws RemoteException {
        finalizarPartida(estado, List.of(usuario1, usuario2), idPartidaGuardada);
    }

    /**
     * Aplica los resultados finales de una partida de 2 o 4 usuarios.
     * NUEVO (Fase 10 - Soporte 2 o 4 jugadores): itera sobre la lista
     * completa de usuarios en lugar de asumir exactamente 2, aplicando a
     * cada uno el ResultadoJugador correspondiente a su mismo índice.
     */
    public void finalizarPartida(Burako estado, List<Usuario> usuarios, String idPartidaGuardada) throws RemoteException {
        List<ResultadoJugador> resultados = estado.getResultados();
        for (int i = 0; i < usuarios.size() && i < resultados.size(); i++) {
            aplicarResultado(usuarios.get(i), resultados.get(i));
        }
        for (Usuario usuario : usuarios) {
            repositorioRanking.actualizar(usuario);
        }

        if (idPartidaGuardada != null) {
            repositorioPartidas.eliminar(idPartidaGuardada);
        }
    }

    private void aplicarResultado(Usuario usuario, ResultadoJugador resultado) {
        usuario.getEstadisticas().registrarResultado(resultado.esGanador(), resultado.getPuntaje());
        repositorioUsuarios.guardar(usuario);
    }

    public List<EntradaRanking> obtenerRanking() {
        return repositorioRanking.obtenerRankingOrdenado();
    }
}
