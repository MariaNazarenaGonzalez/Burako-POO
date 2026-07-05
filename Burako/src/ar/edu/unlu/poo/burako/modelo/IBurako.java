package ar.edu.unlu.poo.burako.modelo;

import ar.edu.unlu.rmimvc.observer.IObservableRemoto;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Interfaz pública del modelo principal de Burako.
 *
 * NUEVA - Introduce el Principio de Inversión de Dependencias (DIP):
 * el Controlador ahora depende de esta abstracción en lugar de la
 * clase concreta Burako. Esto permite:
 * - Sustituir la implementación por un stub RMI en el futuro.
 * - Testear el Controlador con un mock sin instanciar el modelo completo.
 * - Definir claramente qué operaciones el Controlador puede invocar.
 *
 * Solo expone operaciones de dominio y tipos de la capa modelo
 * (FichaMostrable, JuegoMostrable, Eventos, EstadoTurno, ResultadoJugador).
 * Nunca expone Ficha, Jugador, Juego ni ningún tipo concreto del modelo.
 *
 * MODIFICADO (Fase 9 - Integración RMIMVC):
 * - Extiende IObservableRemoto (librería RMIMVC de la cátedra) en lugar de
 *   nuestra interfaz local Observado. Es el cambio obligatorio que documenta
 *   el README de la librería: "Crear interface con los métodos públicos del
 *   modelo y hacer que la interfaz extienda de IObservableRemoto". Como
 *   IObservableRemoto ya declara agregarObservador/removerObservador/
 *   notificarObservadores, IBurako conserva exactamente el mismo rol que
 *   tenía Observado (registrar y notificar observadores), solo que ahora
 *   con soporte para invocación remota.
 * - Cada método declara "throws RemoteException": al ejecutarse el modelo
 *   en el servidor y ser invocado a través de la red, cualquier llamada
 *   puede fallar por motivos de comunicación. Es un requisito mecánico de
 *   RMI, no un cambio de comportamiento del dominio.
 */
public interface IBurako extends IObservableRemoto {

    // ── Configuración ──────────────────────────────────────────────────────────

    /** Asigna los nombres de los dos jugadores antes de comenzar. */
    void setNombres(String nombreJugador1, String nombreJugador2) throws RemoteException;

    /**
     * Asigna los nombres de todos los jugadores de la partida (2 o 4),
     * en orden de índice. Si la lista es más corta que la cantidad real
     * de jugadores, los índices restantes conservan su nombre por defecto.
     *
     * NUEVO (Fase 10 - Soporte 2 o 4 jugadores).
     */
    void setNombres(List<String> nombres) throws RemoteException;

    /** Retorna la cantidad de jugadores de esta partida (2 o 4). NUEVO (Fase 10). */
    int getCantidadJugadores() throws RemoteException;

    /**
     * Retorna el índice de equipo (0 o 1) del jugador dado. Con 2 jugadores,
     * cada uno es su propio equipo. Con 4, los jugadores 0 y 2 comparten
     * equipo, y 1 y 3 comparten el otro. NUEVO (Fase 10).
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
