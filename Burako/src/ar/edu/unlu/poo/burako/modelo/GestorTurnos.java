package ar.edu.unlu.poo.burako.modelo;

/**
 * Gestiona el estado del turno dentro de una partida de Burako.
 *
 * NUEVA - Extraída de Burako para separar la responsabilidad de "quién juega
 * y en qué fase está" del resto de la orquestación.
 *
 * Anteriormente Burako contenía:
 *   private int turnoActual;
 *   private EstadoTurno estadoTurno;
 *   private void validarTurno(int turno) throws Exception { ... }
 *   private void validarEstado(EstadoTurno e, String msg) throws Exception { ... }
 *   private void avanzarTurno() { ... }
 *   public boolean puedeTomar(int turno) { ... }
 *   public boolean puedeJugar(int turno) { ... }
 *
 * Toda esa lógica vive ahora aquí, cohesionada en una sola clase con
 * esa única responsabilidad.
 */
class GestorTurnos {

    private int turnoActual;
    private EstadoTurno estado;
    private final int cantidadJugadores;

    GestorTurnos(int cantidadJugadores) {
        this.cantidadJugadores = cantidadJugadores;
        this.turnoActual = 0;
        this.estado = EstadoTurno.TOMAR;
    }

    // ── Consultas ──────────────────────────────────────────────────────────────

    int getTurnoActual() {
        return turnoActual;
    }

    EstadoTurno getEstado() {
        return estado;
    }

    boolean puedeTomar(int indiceJugador) {
        return turnoActual == indiceJugador && estado == EstadoTurno.TOMAR;
    }

    boolean puedeJugar(int indiceJugador) {
        return turnoActual == indiceJugador && estado == EstadoTurno.JUGAR;
    }

    boolean esFinDePartida() {
        return estado == EstadoTurno.PARTIDA_TERMINADA;
    }

    // ── Transiciones ───────────────────────────────────────────────────────────

    /**
     * Valida que sea el turno del jugador indicado.
     * @throws Exception si no es su turno.
     */
    void validarEsTurno(int indiceJugador) throws Exception {
        if (turnoActual != indiceJugador) {
            throw new Exception("No es el turno del jugador " + indiceJugador + ".");
        }
    }

    /**
     * Valida que el estado del turno sea el esperado.
     * @throws Exception con el mensaje dado si el estado no coincide.
     */
    void validarEstado(EstadoTurno esperado, String mensajeError) throws Exception {
        if (estado != esperado) {
            throw new Exception(mensajeError);
        }
    }

    /** Marca que el jugador ya tomó su ficha: pasa de TOMAR → JUGAR. */
    void marcarFichaTomada() {
        estado = EstadoTurno.JUGAR;
    }

    /** Avanza al siguiente jugador y reinicia el estado a TOMAR. */
    void avanzarTurno() {
        turnoActual = (turnoActual + 1) % cantidadJugadores;
        estado = EstadoTurno.TOMAR;
    }

    /** Finaliza la partida. */
    void finalizarPartida() {
        estado = EstadoTurno.PARTIDA_TERMINADA;
    }
}
