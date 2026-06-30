package ar.edu.unlu.poo.burako.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquestador principal del modelo de Burako.
 * Implementa IBurako (la interfaz que expone el Controlador).
 *
 * Responsabilidad única: coordinar los colaboradores estructurales
 * (GestorTurnos, GestorMuertos, Jugador, Mazo, Pozo) usando ReglasDeJuego
 * como árbitro de todas las decisiones de negocio.
 *
 * Flujo de cada acción pública:
 *   1. Construir ContextoJugada con el estado actual relevante.
 *   2. Consultar ReglasDeJuego.validarXxx(ctx) → ResultadoValidacion.
 *   3a. Si válido:   ejecutar la operación estructural, notificar evento exitoso.
 *   3b. Si inválido: registrar mensaje, notificar evento _NO_exitoso.
 *
 * Burako NO contiene ninguna validación de negocio propia.
 */
public class Burako implements IBurako {

    private final List<Jugador> jugadores;
    private final Mazo          mazo;
    private final Pozo          pozo;
    private final GestorTurnos  gestorTurnos;
    private final GestorMuertos gestorMuertos;

    private final List<Observador> observadores       = new ArrayList<>();
    private String                 ultimoMensajeError = "";

    public Burako() {
        mazo = new Mazo();
        pozo = new Pozo();

        List<Ficha> fichasMuerto1 = mazo.sacar(11);
        List<Ficha> fichasMuerto2 = mazo.sacar(11);
        gestorMuertos = new GestorMuertos(fichasMuerto1, fichasMuerto2);

        jugadores = new ArrayList<>();
        jugadores.add(new Jugador(mazo.sacar(12)));
        jugadores.add(new Jugador(mazo.sacar(12)));

        gestorTurnos = new GestorTurnos(jugadores.size());
    }

    // ── Observado ─────────────────────────────────────────────────────────────

    @Override
    public void agregarObservador(Observador observador) {
        observadores.add(observador);
    }

    @Override
    public void notificarObservadores(Eventos evento) {
        for (Observador obs : observadores) obs.notificar(evento);
    }

    // ── Configuración ─────────────────────────────────────────────────────────

    @Override
    public void setNombres(String nombre1, String nombre2) {
        jugadores.get(0).setNombre(nombre1);
        jugadores.get(1).setNombre(nombre2);
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    @Override public int         getTurnoActual()        { return gestorTurnos.getTurnoActual(); }
    @Override public EstadoTurno getEstadoTurno()        { return gestorTurnos.getEstado(); }
    @Override public String      getNombreJugador(int i) { return jugadores.get(i).getNombre(); }
    @Override public String      getUltimoMensajeError() { return ultimoMensajeError; }

    @Override
    public boolean puedeTomar(int indice) {
        return gestorTurnos.getTurnoActual() == indice
                && gestorTurnos.getEstado() == EstadoTurno.TOMAR;
    }

    @Override
    public boolean puedeJugar(int indice) {
        return gestorTurnos.getTurnoActual() == indice
                && gestorTurnos.getEstado() == EstadoTurno.JUGAR;
    }

    @Override public List<FichaMostrable> getPozo()         { return pozo.get(); }
    @Override public List<FichaMostrable> getAtril(int i)   { return jugadores.get(i).getAtril(); }
    @Override public List<JuegoMostrable> getJuegos(int i)  { return jugadores.get(i).getJugadas(); }
    @Override public int                  cantJuegos(int i) { return jugadores.get(i).cantJuegos(); }

    @Override
    public List<ResultadoJugador> getResultados() {
        int quien = gestorTurnos.getTurnoActual();
        List<ResultadoJugador> resultados = new ArrayList<>();
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j   = jugadores.get(i);
            int puntaje = j.calcularPuntaje(i == quien);
            resultados.add(new ResultadoJugador(j.getNombre(), puntaje, i == quien));
        }
        return resultados;
    }

    // ── ACCIÓN: Robo del mazo ─────────────────────────────────────────────────

    @Override
    public boolean agarrarMazo(int indice) {
        ContextoJugada ctx = contextoBase(indice)
                .mazoVacio(mazo.estaVacio())
                .build();

        ResultadoValidacion r = ReglasDeJuego.validarRoboMazo(ctx);
        if (!r.esValido()) return fallar(r.getMensaje(), Eventos.tomarMazo_NO_exitoso);

        jugadores.get(indice).agregarAtril(mazo.sacarFicha());
        gestorTurnos.marcarFichaTomada();
        notificarObservadores(Eventos.tomarMazo_exitoso);
        return true;
    }

    // ── ACCIÓN: Robo del pozo ─────────────────────────────────────────────────

    @Override
    public boolean agarrarPozo(int indice) {
        ContextoJugada ctx = contextoBase(indice)
                .pozoVacio(pozo.estaVacio())
                .build();

        ResultadoValidacion r = ReglasDeJuego.validarRoboPozo(ctx);
        if (!r.esValido()) return fallar(r.getMensaje(), Eventos.tomarPozo_NO_exitoso);

        jugadores.get(indice).agregarAtril(pozo.tomar());
        gestorTurnos.marcarFichaTomada();
        notificarObservadores(Eventos.tomarPozo_exitoso);
        return true;
    }

    // ── ACCIÓN: Bajar juego ───────────────────────────────────────────────────

    @Override
    public void bajarJuego(int indice, int[] posicionesAtril) {
        Jugador jugador = jugadores.get(indice);

        // Extraer fichas para pasarlas al validador
        List<Ficha> seleccionadas;
        try {
            seleccionadas = fichasEnPosiciones(jugador, posicionesAtril);
        } catch (Exception e) {
            fallar(e.getMessage(), Eventos.bajarJuego_NO_exitoso);
            return;
        }

        ContextoJugada ctx = contextoBase(indice)
                .fichasSeleccionadas(seleccionadas)
                .build();

        ResultadoValidacion r = ReglasDeJuego.validarBajarJuego(ctx);
        if (!r.esValido()) {
            fallar(r.getMensaje(), Eventos.bajarJuego_NO_exitoso);
            return;
        }

        try {
            jugador.bajarJuego(posicionesAtril);
        } catch (Exception e) {
            fallar(e.getMessage(), Eventos.bajarJuego_NO_exitoso);
            return;
        }

        procesarTomaMuertaDirecta(jugador, indice);
        notificarObservadores(Eventos.bajarJuego_exitoso);
    }

    // ── ACCIÓN: Apoyar juego ──────────────────────────────────────────────────

    @Override
    public void apoyarJuego(int posAtril, int posJuego, int indice, int numJuego) {
        Jugador jugador = jugadores.get(indice);

        Ficha ficha;
        try {
            ficha = jugador.verAtril(posAtril);
        } catch (Exception e) {
            fallar(e.getMessage(), Eventos.apoyarJuego_NO_exitoso);
            return;
        }

        List<Ficha> fichasDelJuego = jugador.getFichasDeJuego(numJuego);
        TipoJuego   tipoDestino    = jugador.getTipoDeJuego(numJuego);

        ContextoJugada ctx = contextoBase(indice)
                .tipoJuegoDestino(tipoDestino)
                .build();

        // posJuego es 1-based; validador usa 0-based para la posición
        ResultadoValidacion r = ReglasDeJuego.validarApoyarJuego(
                ctx, ficha, posJuego - 1, fichasDelJuego);
        if (!r.esValido()) {
            fallar(r.getMensaje(), Eventos.apoyarJuego_NO_exitoso);
            return;
        }

        try {
            jugador.apoyarJuego(posAtril, posJuego, numJuego);
        } catch (Exception e) {
            fallar(e.getMessage(), Eventos.apoyarJuego_NO_exitoso);
            return;
        }

        procesarTomaMuertaDirecta(jugador, indice);
        notificarObservadores(Eventos.apoyarJuego_exitoso);
    }

    // ── ACCIÓN: Descartar al pozo ─────────────────────────────────────────────

    @Override
    public void agregarPozo(int posAtril, int indice) {
        Jugador jugador = jugadores.get(indice);

        // Paso 1: validar que puede descartar (turno + estado JUGAR)
        ContextoJugada ctxDescarte = contextoBase(indice).build();
        ResultadoValidacion rd = ReglasDeJuego.validarDescarte(ctxDescarte);
        if (!rd.esValido()) {
            fallar(rd.getMensaje(), Eventos.agregarPozo_NO_exitoso);
            return;
        }

        // Paso 2: extraer la ficha del atril
        Ficha ficha;
        try {
            ficha = jugador.verAtril(posAtril);
            jugador.sacarAtril(ficha);
        } catch (Exception e) {
            fallar(e.getMessage(), Eventos.agregarPozo_NO_exitoso);
            return;
        }

        pozo.agregar(ficha);

        // Paso 3: si el atril quedó vacío, evaluar consecuencias
        if (jugador.atrilVacio()) {
            ContextoJugada ctxFinal = contextoConJugador(indice, jugador);
            ResultadoValidacion rf = ReglasDeJuego.validarDescarteFinal(ctxFinal);

            if (!rf.esValido()) {
                // Revertir: devolver la ficha al atril
                jugador.agregarAtril(pozo.tomar());
                fallar(rf.getMensaje(), Eventos.agregarPozo_NO_exitoso);
                return;
            }

            if (ReglasDeJuego.correspondeTomaMuertoIndirecta(ctxFinal)) {
                gestorMuertos.asignarMuerto(jugador);
                gestorTurnos.avanzarTurno();
                notificarObservadores(Eventos.agregarPozo_exitoso);
                notificarObservadores(Eventos.tomarMuerto_exitoso);
            } else if (ReglasDeJuego.puedeCortar(ctxFinal)) {
                gestorTurnos.finalizarPartida();
                notificarObservadores(Eventos.agregarPozo_exitoso);
                notificarObservadores(Eventos.cortar_exitoso);
                notificarObservadores(Eventos.partida_terminada);
            } else {
                // Atril vacío sin muerto ni corte: avanzar turno normalmente
                gestorTurnos.avanzarTurno();
                notificarObservadores(Eventos.agregarPozo_exitoso);
            }
        } else {
            gestorTurnos.avanzarTurno();
            notificarObservadores(Eventos.agregarPozo_exitoso);
        }
    }

    // ── Acceso directo a Jugador (solo para tests) ────────────────────────────

    /** El Controlador NO debe usar este método. Solo para tests existentes. */
    public Jugador getJugador(int indice) {
        return jugadores.get(indice);
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    /**
     * Construye el contexto base con turno actual y estado del jugador indicado.
     * Retorna el Builder para que el llamador agregue campos específicos de la acción.
     */
    private ContextoJugada.Builder contextoBase(int indice) {
        Jugador j = jugadores.get(indice);
        return ContextoJugada.builder()
                .turnoActual(gestorTurnos.getTurnoActual())
                .estadoTurno(gestorTurnos.getEstado())
                .indiceJugador(indice)
                .cantFichasAtril(j.getAtril().size())
                .yaTomoMuerto(j.yaTomoMuerto())
                .tieneCanasta(j.tieneCanasta())
                .cantJuegos(j.cantJuegos())
                .pozoVacio(pozo.estaVacio())
                .mazoVacio(mazo.estaVacio())
                .hayMuertosDisponibles(gestorMuertos.hayMuertosDisponibles());
    }

    /**
     * Contexto con el estado ACTUAL del jugador (post-operación).
     * Usado para evaluar descarte final DESPUÉS de haber removido la ficha del atril.
     */
    private ContextoJugada contextoConJugador(int indice, Jugador j) {
        return ContextoJugada.builder()
                .turnoActual(gestorTurnos.getTurnoActual())
                .estadoTurno(gestorTurnos.getEstado())
                .indiceJugador(indice)
                .cantFichasAtril(j.getAtril().size())
                .yaTomoMuerto(j.yaTomoMuerto())
                .tieneCanasta(j.tieneCanasta())
                .cantJuegos(j.cantJuegos())
                .pozoVacio(pozo.estaVacio())
                .mazoVacio(mazo.estaVacio())
                .hayMuertosDisponibles(gestorMuertos.hayMuertosDisponibles())
                .build();
    }

    /**
     * Si el atril quedó vacío tras bajar/apoyar y corresponde toma directa de muerto,
     * la ejecuta. El turno NO avanza en la toma directa.
     */
    private void procesarTomaMuertaDirecta(Jugador jugador, int indice) {
        ContextoJugada ctx = contextoConJugador(indice, jugador);
        if (ReglasDeJuego.correspondeTomaMuertaDirecta(ctx)) {
            gestorMuertos.asignarMuerto(jugador);
            notificarObservadores(Eventos.tomarMuerto_exitoso);
        }
    }

    /**
     * Extrae las fichas del atril del jugador en las posiciones 1-based dadas,
     * sin removerlas. Para construir el ContextoJugada antes de validar.
     */
    private List<Ficha> fichasEnPosiciones(Jugador jugador, int[] posiciones) throws Exception {
        List<FichaMostrable> atril = jugador.getAtril();
        List<Ficha> resultado = new ArrayList<>();
        for (int pos : posiciones) {
            if (pos < 1 || pos > atril.size()) {
                throw new Exception("Posición " + pos + " fuera del atril (tamaño: " + atril.size() + ").");
            }
            resultado.add((Ficha) atril.get(pos - 1));
        }
        return resultado;
    }

    private boolean fallar(String mensaje, Eventos evento) {
        ultimoMensajeError = mensaje != null ? mensaje : "Error desconocido.";
        notificarObservadores(evento);
        return false;
    }
}