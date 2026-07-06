package ar.edu.unlu.poo.burako.modelo;

import ar.edu.unlu.rmimvc.observer.IObservableRemoto;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Define la interfaz pública del modelo del juego Burako.
 *
 * A través de esta interfaz el controlador puede consultar el
 * estado de la partida y ejecutar las acciones permitidas por
 * las reglas del juego, sin depender de la implementación
 * concreta del modelo.
 *
 * La interfaz también permite notificar cambios de estado a los
 * observadores mediante el mecanismo proporcionado por RMIMVC,
 * facilitando tanto la ejecución local como remota de la
 * aplicación.
 */
public interface IBurako extends IObservableRemoto {

    // ── Configuración ──────────────────────────────────────────────────────────

    /** Asigna los nombres de los dos jugadores antes de comenzar. */
    void setNombres(String nombreJugador1, String nombreJugador2) throws RemoteException;

    /**
     * Asigna los nombres de todos los jugadores de la partida (2 o 4),
     * en orden de índice. Si la lista es más corta que la cantidad real
     * de jugadores, los índices restantes conservan su nombre por defecto.
     */
    void setNombres(List<String> nombres) throws RemoteException;

    /** Retorna la cantidad de jugadores de esta partida (2 o 4). */
    int getCantidadJugadores() throws RemoteException;

    /**
     * Retorna el índice de equipo (0 o 1) del jugador dado. Con 2 jugadores,
     * cada uno es su propio equipo. Con 4, los jugadores 0 y 2 comparten
     * equipo, y 1 y 3 comparten el otro..
     */
    int getEquipo(int indiceJugador) throws RemoteException;

    // ── Consultas de estado ────────────────────────────────────────────────────

    /** Retorna el índice (0-based) del jugador cuyo turno está activo. */
    int getTurnoActual() throws RemoteException;

    /** Retorna el estado actual del turno (TOMAR, JUGAR, PARTIDA_TERMINADA). */
    EstadoTurno getEstadoTurno() throws RemoteException;

    /** Retorna true si el jugador {@code indice} puede tomar del mazo o del pozo. */
    boolean puedeTomar(int indice) throws RemoteException;

    /** Retorna true si el jugador {@code indice} puede bajar, apoyar o descartar. */
    boolean puedeJugar(int indice) throws RemoteException;

    /** Retorna el nombre del jugador {@code indice}. */
    String getNombreJugador(int indice) throws RemoteException;

    /**
     * Retorna el último mensaje de error generado por una operación fallida.
     * La Vista lo consulta después de recibir un evento _NO_exitoso.
     * Nunca retorna null; retorna cadena vacía si no hay error pendiente.
     */
    String getUltimoMensajeError() throws RemoteException;

    // ── Consultas de datos de juego ────────────────────────────────────────────

    /** Retorna las fichas visibles del pozo. */
    List<FichaMostrable> getPozo() throws RemoteException;

    /** Retorna las fichas del atril del jugador {@code indice}. */
    List<FichaMostrable> getAtril(int indice) throws RemoteException;

    /** Retorna los juegos bajados por el jugador {@code indice}. */
    List<JuegoMostrable> getJuegos(int indice) throws RemoteException;

    /** Retorna la cantidad de juegos bajados por el jugador {@code indice}. */
    int cantJuegos(int indice) throws RemoteException;

    /**
     * Retorna los resultados finales de todos los jugadores.
     * Solo disponible cuando getEstadoTurno() == PARTIDA_TERMINADA.
     */
    List<ResultadoJugador> getResultados() throws RemoteException;

    // ── Acciones del turno ─────────────────────────────────────────────────────

    /**
     * El jugador {@code indice} toma una ficha del mazo.
     * @return true si la operación fue exitosa.
     */
    boolean agarrarMazo(int indice) throws RemoteException;

    /**
     * El jugador {@code indice} toma todas las fichas del pozo.
     * @return true si la operación fue exitosa.
     */
    boolean agarrarPozo(int indice) throws RemoteException;

    /**
     * El jugador {@code indice} baja un juego nuevo con las fichas
     * en las posiciones indicadas (1-based) de su atril.
     */
    void bajarJuego(int indice, int[] posicionesAtril) throws RemoteException;

    /**
     * El jugador {@code indice} apoya la ficha en la posición {@code posAtril} (1-based)
     * de su atril sobre el juego número {@code numJuego} (1-based) en la posición
     * {@code posJuego} (1-based) de ese juego.
     */
    void apoyarJuego(int posAtril, int posJuego, int indice, int numJuego) throws RemoteException;

    /**
     * El jugador {@code indice} descarta la ficha en la posición {@code posAtril} (1-based)
     * al pozo, cediendo el turno o cerrando la partida si corresponde.
     */
    void agregarPozo(int posAtril, int indice) throws RemoteException;
}
