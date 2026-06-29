package ar.edu.unlu.poo.burako.modelo;

import java.util.List;

/**
 * Instantánea inmutable del contexto de una jugada, usada por ReglasDeJuego.
 *
 * ReglasDeJuego no recibe referencias directas a Jugador, Pozo, Mazo ni Atril.
 * Recibe un ContextoJugada con exactamente los datos que necesita para validar,
 * sin depender de estructuras internas.
 *
 * Esto permite:
 * - Testear cada regla de forma aislada con datos construidos ad-hoc.
 * - Que ReglasDeJuego sea independiente de la implementación de Jugador o Burako.
 * - Que en el futuro un cliente RMI construya un ContextoJugada con datos
 *   recibidos por red y lo valide localmente sin tocar el modelo del servidor.
 */
public final class ContextoJugada {

    // ── Estado del turno ──────────────────────────────────────────────────────
    private final int         turnoActual;
    private final EstadoTurno estadoTurno;
    private final int         indiceJugador;

    // ── Estado del jugador ────────────────────────────────────────────────────
    private final int     cantFichasAtril;
    private final boolean yaTomoMuerto;
    private final boolean tieneCanasta;
    private final int     cantJuegos;

    // ── Estado de la mesa ─────────────────────────────────────────────────────
    private final boolean pozoVacio;
    private final boolean mazoVacio;
    private final boolean hayMuertosDisponibles;

    // ── Acción específica ─────────────────────────────────────────────────────
    private final List<Ficha> fichasSeleccionadas; // para bajar/apoyar
    private final TipoJuego   tipoJuegoDestino;    // para apoyar (puede ser null)

    private ContextoJugada(Builder b) {
        this.turnoActual           = b.turnoActual;
        this.estadoTurno           = b.estadoTurno;
        this.indiceJugador         = b.indiceJugador;
        this.cantFichasAtril       = b.cantFichasAtril;
        this.yaTomoMuerto          = b.yaTomoMuerto;
        this.tieneCanasta          = b.tieneCanasta;
        this.cantJuegos            = b.cantJuegos;
        this.pozoVacio             = b.pozoVacio;
        this.mazoVacio             = b.mazoVacio;
        this.hayMuertosDisponibles = b.hayMuertosDisponibles;
        this.fichasSeleccionadas   = b.fichasSeleccionadas;
        this.tipoJuegoDestino      = b.tipoJuegoDestino;
    }

    public int         getTurnoActual()           { return turnoActual; }
    public EstadoTurno getEstadoTurno()           { return estadoTurno; }
    public int         getIndiceJugador()         { return indiceJugador; }
    public int         getCantFichasAtril()       { return cantFichasAtril; }
    public boolean     yaTomoMuerto()             { return yaTomoMuerto; }
    public boolean     tieneCanasta()             { return tieneCanasta; }
    public int         getCantJuegos()            { return cantJuegos; }
    public boolean     isPozoVacio()              { return pozoVacio; }
    public boolean     isMazoVacio()              { return mazoVacio; }
    public boolean     hayMuertosDisponibles()    { return hayMuertosDisponibles; }
    public List<Ficha> getFichasSeleccionadas()   { return fichasSeleccionadas; }
    public TipoJuego   getTipoJuegoDestino()      { return tipoJuegoDestino; }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int         turnoActual           = 0;
        private EstadoTurno estadoTurno           = EstadoTurno.TOMAR;
        private int         indiceJugador         = 0;
        private int         cantFichasAtril       = 0;
        private boolean     yaTomoMuerto          = false;
        private boolean     tieneCanasta          = false;
        private int         cantJuegos            = 0;
        private boolean     pozoVacio             = true;
        private boolean     mazoVacio             = false;
        private boolean     hayMuertosDisponibles = false;
        private List<Ficha> fichasSeleccionadas   = List.of();
        private TipoJuego   tipoJuegoDestino      = null;

        public Builder turnoActual(int v)                  { turnoActual = v; return this; }
        public Builder estadoTurno(EstadoTurno v)          { estadoTurno = v; return this; }
        public Builder indiceJugador(int v)                { indiceJugador = v; return this; }
        public Builder cantFichasAtril(int v)              { cantFichasAtril = v; return this; }
        public Builder yaTomoMuerto(boolean v)             { yaTomoMuerto = v; return this; }
        public Builder tieneCanasta(boolean v)             { tieneCanasta = v; return this; }
        public Builder cantJuegos(int v)                   { cantJuegos = v; return this; }
        public Builder pozoVacio(boolean v)                { pozoVacio = v; return this; }
        public Builder mazoVacio(boolean v)                { mazoVacio = v; return this; }
        public Builder hayMuertosDisponibles(boolean v)    { hayMuertosDisponibles = v; return this; }
        public Builder fichasSeleccionadas(List<Ficha> v)  { fichasSeleccionadas = v; return this; }
        public Builder tipoJuegoDestino(TipoJuego v)       { tipoJuegoDestino = v; return this; }

        public ContextoJugada build() {
            return new ContextoJugada(this);
        }
    }
}
