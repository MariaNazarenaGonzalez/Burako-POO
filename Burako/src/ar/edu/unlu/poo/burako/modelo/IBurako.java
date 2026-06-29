package ar.edu.unlu.poo.burako.modelo;

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
 */
public interface IBurako extends Observado {

    // ── Configuración ──────────────────────────────────────────────────────────

    /** Asigna los nombres de los dos jugadores antes de comenzar. */
    void setNombres(String nombreJugador1, String nombreJugador2);

    // ── Consultas de estado ────────────────────────────────────────────────────

    /** Retorna el índice (0-based) del jugador cuyo turno está activo. */
    int getTurnoActual();

    /** Retorna el estado actual del turno (TOMAR, JUGAR, PARTIDA_TERMINADA). */
    EstadoTurno getEstadoTurno();

    /** Retorna true si el jugador {@code indice} puede tomar del mazo o del pozo. */
    boolean puedeTomar(int indice);

    /** Retorna true si el jugador {@code indice} puede bajar, apoyar o descartar. */
    boolean puedeJugar(int indice);

    /** Retorna el nombre del jugador {@code indice}. */
    String getNombreJugador(int indice);

    /**
     * Retorna el último mensaje de error generado por una operación fallida.
     * La Vista lo consulta después de recibir un evento _NO_exitoso.
     * Nunca retorna null; retorna cadena vacía si no hay error pendiente.
     */
    String getUltimoMensajeError();

    // ── Consultas de datos de juego ────────────────────────────────────────────

    /** Retorna las fichas visibles del pozo. */
    List<FichaMostrable> getPozo();

    /** Retorna las fichas del atril del jugador {@code indice}. */
    List<FichaMostrable> getAtril(int indice);

    /** Retorna los juegos bajados por el jugador {@code indice}. */
    List<JuegoMostrable> getJuegos(int indice);

    /** Retorna la cantidad de juegos bajados por el jugador {@code indice}. */
    int cantJuegos(int indice);

    /**
     * Retorna los resultados finales de todos los jugadores.
     * Solo disponible cuando getEstadoTurno() == PARTIDA_TERMINADA.
     */
    List<ResultadoJugador> getResultados();

    // ── Acciones del turno ─────────────────────────────────────────────────────

    /**
     * El jugador {@code indice} toma una ficha del mazo.
     * @return true si la operación fue exitosa.
     */
    boolean agarrarMazo(int indice);

    /**
     * El jugador {@code indice} toma todas las fichas del pozo.
     * @return true si la operación fue exitosa.
     */
    boolean agarrarPozo(int indice);

    /**
     * El jugador {@code indice} baja un juego nuevo con las fichas
     * en las posiciones indicadas (1-based) de su atril.
     */
    void bajarJuego(int indice, int[] posicionesAtril);

    /**
     * El jugador {@code indice} apoya la ficha en la posición {@code posAtril} (1-based)
     * de su atril sobre el juego número {@code numJuego} (1-based) en la posición
     * {@code posJuego} (1-based) de ese juego.
     */
    void apoyarJuego(int posAtril, int posJuego, int indice, int numJuego);

    /**
     * El jugador {@code indice} descarta la ficha en la posición {@code posAtril} (1-based)
     * al pozo, cediendo el turno o cerrando la partida si corresponde.
     */
    void agregarPozo(int posAtril, int indice);
}