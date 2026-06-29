package ar.edu.unlu.poo.burako.modelo;

/**
 * Estado del turno dentro de una partida de Burako.
 * Sin cambios de comportamiento respecto al original.
 */
public enum EstadoTurno {
    /** El jugador con el turno debe tomar una ficha (del mazo o del pozo). */
    TOMAR,
    /** El jugador ya tomó su ficha y puede bajar juegos, apoyar o descartar al pozo. */
    JUGAR,
    /** La partida ha finalizado (alguien cortó). */
    PARTIDA_TERMINADA
}
