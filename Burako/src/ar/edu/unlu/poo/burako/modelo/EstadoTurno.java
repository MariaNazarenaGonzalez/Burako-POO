package ar.edu.unlu.poo.burako.modelo;

/**
 * Representa las distintas etapas que puede atravesar
 * el turno de un jugador durante una partida.
 */
public enum EstadoTurno {
    /** El jugador con el turno debe tomar una ficha (del mazo o del pozo). */
    TOMAR,
    /**
     * El jugador ya tomó una ficha y puede realizar
     * jugadas, apoyar fichas o finalizar su turno.
     */
    JUGAR,
    /**
     * Indica que la partida terminó luego de que un
     * jugador cumpliera las condiciones de corte.
     */
    PARTIDA_TERMINADA
}
