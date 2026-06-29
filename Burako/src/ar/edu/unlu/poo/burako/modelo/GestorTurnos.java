package ar.edu.unlu.poo.burako.modelo;

/**
 * Gestiona el estado del turno: quién juega y en qué fase está.
 *
 * Responsabilidad única: mantener y transicionar el estado (turno + fase).
 * NO contiene validaciones de negocio. Las validaciones de "¿puede este
 * jugador hacer esta acción?" son responsabilidad exclusiva de ReglasDeJuego.
 *
 * GestorTurnos solo expone transiciones: marcarFichaTomada(), avanzarTurno(),
 * finalizarPartida(). Burako las invoca DESPUÉS de que ReglasDeJuego aprobó
 * la acción.
 */
class GestorTurnos {

    private int         turnoActual;
    private EstadoTurno estado;
    private final int   cantidadJugadores;

    GestorTurnos(int cantidadJugadores) {
        this.cantidadJugadores = cantidadJugadores;
        this.turnoActual       = 0;
        this.estado            = EstadoTurno.TOMAR;
    }

    // ── Consultas ──────────────────────────────────────────────────────────────

    int getTurnoActual() {
        return turnoActual;
    }

    EstadoTurno getEstado() {
        return estado;
    }

    // ── Transiciones (solo las invoca Burako, tras validar con ReglasDeJuego) ──

    /** TOMAR → JUGAR: el jugador tomó su ficha. */
    void marcarFichaTomada() {
        estado = EstadoTurno.JUGAR;
    }

    /** JUGAR → TOMAR (del siguiente jugador): fin del turno actual. */
    void avanzarTurno() {
        turnoActual = (turnoActual + 1) % cantidadJugadores;
        estado      = EstadoTurno.TOMAR;
    }

    /** → PARTIDA_TERMINADA: la partida finalizó. */
    void finalizarPartida() {
        estado = EstadoTurno.PARTIDA_TERMINADA;
    }
}
