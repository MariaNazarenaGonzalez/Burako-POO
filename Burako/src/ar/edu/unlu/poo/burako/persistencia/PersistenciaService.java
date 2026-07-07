package ar.edu.unlu.poo.burako.persistencia;

import ar.edu.unlu.poo.burako.modelo.Burako;
import ar.edu.unlu.poo.burako.modelo.ResultadoJugador;

import java.io.File;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

/**
 * Centraliza las operaciones de persistencia de la aplicación.
 *
 * Coordina el acceso a los repositorios de usuarios, partidas y ranking,
 * ofreciendo una interfaz unificada para registrar usuarios, guardar y
 * recuperar partidas, actualizar estadísticas y consultar el ranking.
 */
public class PersistenciaService {

    private final IRepositorioUsuarios repositorioUsuarios;
    private final IRepositorioRanking  repositorioRanking;
    private final IRepositorioPartidas repositorioPartidas;

    /**
     * Inicializa el servicio utilizando un directorio base para almacenar
     * toda la información persistente.
     */
    public PersistenciaService(String directorioBase) {
        File base = new File(directorioBase);
        this.repositorioUsuarios = new RepositorioUsuarios(new File(base, "usuarios.dat"));
        this.repositorioRanking  = new RepositorioRanking(new File(base, "ranking.dat"));
        this.repositorioPartidas = new RepositorioPartidas(new File(base, "partidas"));
    }

    /**
     * Inicializa el servicio utilizando implementaciones de repositorios
     * proporcionadas externamente.
     */
    public PersistenciaService(IRepositorioUsuarios repositorioUsuarios,
                                IRepositorioRanking repositorioRanking,
                                IRepositorioPartidas repositorioPartidas) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.repositorioRanking  = repositorioRanking;
        this.repositorioPartidas = repositorioPartidas;
    }

    // ── Usuarios ─────────────────────────────────────────────────────────────

    /**
     * Obtiene un usuario existente o registra uno nuevo cuando el nombre
     * aún no se encuentra almacenado.
     */
    public Usuario obtenerOcrearUsuario(String nombre) {
        return repositorioUsuarios.buscarPorNombre(nombre)
                .orElseGet(() -> repositorioUsuarios.registrar(nombre));
    }

    public List<Usuario> listarUsuarios() {
        return repositorioUsuarios.listarTodos();
    }

    // ── Partidas ─────────────────────────────────────────────────────────────

    /**
     * Guarda el estado de una partida asociada a dos usuarios y devuelve
     * el identificador con el que fue almacenada.
     */
    public String guardarPartida(String idPartida, Burako estado, Usuario usuario1, Usuario usuario2) {
        return guardarPartida(idPartida, estado, List.of(usuario1, usuario2));
    }

    /**
     * Guarda el estado de una partida asociada a los usuarios indicados y
     * devuelve el identificador correspondiente.
     */
    public String guardarPartida(String idPartida, Burako estado, List<Usuario> usuarios) {
        String id = idPartida != null ? idPartida : UUID.randomUUID().toString();
        repositorioPartidas.guardar(new PartidaGuardada(id, usuarios, estado));
        return id;
    }

    /**
     * Recupera el estado almacenado de una partida para continuarla desde
     * el punto en que fue guardada.
     */
    public Burako continuarPartida(String idPartida) {
        return repositorioPartidas.buscarPorId(idPartida)
                .orElseThrow(() -> new PersistenciaException("No existe una partida guardada con id " + idPartida))
                .getEstado();
    }

    /**
     * Devuelve las partidas guardadas en las que participa el usuario indicado.
     */
    public List<PartidaGuardada> listarPartidasDe(Usuario usuario) {
        return repositorioPartidas.listarPorUsuario(usuario.getId());
    }

    /**
    /**
     * Devuelve todas las partidas almacenadas.
     */
    public List<PartidaGuardada> listarPartidasGuardadas() {
        return repositorioPartidas.listarTodas();
    }

    // ── Cierre de partida y ranking ─────────────────────────────────────────

    /**
     * Actualiza la información persistente correspondiente a una partida
     * finalizada entre dos usuarios.
     */
    public void finalizarPartida(Burako estado, Usuario usuario1, Usuario usuario2, String idPartidaGuardada) throws RemoteException {
        finalizarPartida(estado, List.of(usuario1, usuario2), idPartidaGuardada);
    }

    /**
     * Actualiza las estadísticas de los participantes, el ranking y elimina
     * el registro de la partida guardada cuando corresponde.
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
    /**
     * Registra el resultado obtenido por un usuario y actualiza su información
     * persistente.
     */
    private void aplicarResultado(Usuario usuario, ResultadoJugador resultado) {
        usuario.getEstadisticas().registrarResultado(resultado.esGanador(), resultado.getPuntaje());
        repositorioUsuarios.guardar(usuario);
    }

    public List<EntradaRanking> obtenerRanking() {
        return repositorioRanking.obtenerRankingOrdenado();
    }
}
