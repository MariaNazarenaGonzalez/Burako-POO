package ar.edu.unlu.poo.burako.modelo;

import java.io.Serializable;

/**
 * Administra el estado del turno durante una partida.
 *
 * Esta clase mantiene el jugador que posee el turno y la etapa
 * actual del mismo, permitiendo realizar las transiciones entre
 * los distintos estados del juego.
 *
 * Su responsabilidad se limita a gestionar el turno; las
 * validaciones sobre si una acción está permitida son realizadas
 * por la lógica de reglas del juego.
 */
class GestorTurnos implements Serializable {

    private static final long serialVersionUID = 1L;

    private int         turnoActual;
    private EstadoTurno estado;
    private final int   cantidadJugadores;
    /**
     * Inicializa el gestor de turnos.
     *
     * La partida comienza con el primer jugador y en la fase de
     * toma de fichas.
     *
     * @param cantidadJugadores cantidad total de jugadores de la partida.
     */
    GestorTurnos(int cantidadJugadores) {
        this.cantidadJugadores = cantidadJugadores;
        this.turnoActual       = 0;
        this.estado            = EstadoTurno.TOMAR;
    }

    // ── Consultas ──────────────────────────────────────────────────────────────
    /**
     * Obtiene el índice del jugador que posee el turno.
     *
     * @return índice del jugador actual.
     */
    int getTurnoActual() {
        return turnoActual;
    }
    /**
     * Obtiene la etapa actual del turno.
     *
     * @return estado del turno.
     */
    EstadoTurno getEstado() {
        return estado;
    }

    // ── Transiciones (solo las invoca Burako, tras validar con ReglasDeJuego) ──

    /**
     * Cambia el estado del turno a la fase de juego,
     * indicando que el jugador ya tomó una ficha.
     */
    void marcarFichaTomada() {
        estado = EstadoTurno.JUGAR;
    }

    /**
     * Finaliza el turno del jugador actual y asigna
     * el turno al siguiente jugador de forma circular.
     */
    void avanzarTurno() {
        turnoActual = (turnoActual + 1) % cantidadJugadores;
        estado      = EstadoTurno.TOMAR;
    }

    /**
     * Marca la partida como finalizada.
     */
    void finalizarPartida() {
        estado = EstadoTurno.PARTIDA_TERMINADA;
    }
}
