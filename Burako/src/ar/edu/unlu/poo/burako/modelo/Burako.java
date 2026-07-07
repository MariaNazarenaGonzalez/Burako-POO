package ar.edu.unlu.poo.burako.modelo;

import ar.edu.unlu.rmimvc.observer.ObservableRemoto;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo principal del juego Burako.
 *
 * Coordina el desarrollo de una partida administrando jugadores, mazo,
 * pozo, turnos y muertos. Cada acción solicitada por el controlador es
 * validada mediante ReglasDeJuego antes de modificar el estado de la
 * partida y notificar los eventos correspondientes a los observadores.
 */
public class Burako extends ObservableRemoto implements IBurako, Serializable {

    private static final long serialVersionUID = 2L;

    private final List<Jugador> jugadores;
    private final Mazo          mazo;
    private final Pozo          pozo;
    private final GestorTurnos  gestorTurnos;
    private final GestorMuertos gestorMuertos;

    private String ultimoMensajeError = "";

    /** Crea una partida con dos jugadores. */
    public Burako() {
        this(2);
    }

    /**
     * @param cantidadJugadores 2 o 4. Con 4, los jugadores se agrupan en
     *                          2 equipos de 2 que comparten únicamente el muerto.
     */
    public Burako(int cantidadJugadores) {
        if (cantidadJugadores != 2 && cantidadJugadores != 4) {
            throw new IllegalArgumentException("Burako solo admite 2 o 4 jugadores.");
        }

        mazo = new Mazo();
        pozo = new Pozo();

        List<Ficha> fichasMuerto1 = mazo.sacar(11);
        List<Ficha> fichasMuerto2 = mazo.sacar(11);
        gestorMuertos = new GestorMuertos(fichasMuerto1, fichasMuerto2);

        int fichasPorMano = cantidadJugadores == 2 ? 12 : 11;
        jugadores = new ArrayList<>();
        for (int i = 0; i < cantidadJugadores; i++) {
            jugadores.add(new Jugador(mazo.sacar(fichasPorMano)));
        }

        gestorTurnos = new GestorTurnos(jugadores.size());
    }

    /**
     * Obtiene el equipo al que pertenece un jugador.
     *
     * En partidas de dos jugadores cada participante constituye su propio
     * equipo. En partidas de cuatro jugadores los equipos se forman con
     * los jugadores 0 y 2, y con los jugadores 1 y 3.
     */
    private int equipoDe(int indiceJugador) {
        return jugadores.size() == 2 ? indiceJugador : indiceJugador % 2;
    }

    // ── Configuración ─────────────────────────────────────────────────────────

    @Override
    public void setNombres(String nombre1, String nombre2) throws RemoteException {
        jugadores.get(0).setNombre(nombre1);
        jugadores.get(1).setNombre(nombre2);
    }

    @Override
    public void setNombres(List<String> nombres) throws RemoteException {
        for (int i = 0; i < nombres.size() && i < jugadores.size(); i++) {
            jugadores.get(i).setNombre(nombres.get(i));
        }
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    @Override public int         getTurnoActual()        throws RemoteException { return gestorTurnos.getTurnoActual(); }
    @Override public EstadoTurno getEstadoTurno()        throws RemoteException { return gestorTurnos.getEstado(); }
    @Override public String      getNombreJugador(int i) throws RemoteException { return jugadores.get(i).getNombre(); }
    @Override public String      getUltimoMensajeError() throws RemoteException { return ultimoMensajeError; }
    @Override public int         getCantidadJugadores()  throws RemoteException { return jugadores.size(); }
    @Override public int         getEquipo(int indice)   throws RemoteException { return equipoDe(indice); }

    @Override
    public boolean puedeTomar(int indice) throws RemoteException {
        return gestorTurnos.getTurnoActual() == indice
                && gestorTurnos.getEstado() == EstadoTurno.TOMAR;
    }

    @Override
    public boolean puedeJugar(int indice) throws RemoteException {
        return gestorTurnos.getTurnoActual() == indice
                && gestorTurnos.getEstado() == EstadoTurno.JUGAR;
    }

    @Override public List<FichaMostrable> getPozo()         throws RemoteException { return pozo.get(); }
    @Override public List<FichaMostrable> getAtril(int i)   throws RemoteException { return jugadores.get(i).getAtril(); }
    @Override public List<JuegoMostrable> getJuegos(int i)  throws RemoteException { return jugadores.get(i).getJugadas(); }
    @Override public int                  cantJuegos(int i) throws RemoteException { return jugadores.get(i).cantJuegos(); }

    @Override
    public List<ResultadoJugador> getResultados() throws RemoteException {
        int quien = gestorTurnos.getTurnoActual();
        int equipoGanador = equipoDe(quien);
        List<ResultadoJugador> resultados = new ArrayList<>();
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            boolean corto = (i == quien);          // el bono de +100 por cortar es individual
            boolean gano  = equipoDe(i) == equipoGanador; // ganar la partida es del equipo completo
            int puntaje = j.calcularPuntaje(corto);
            resultados.add(new ResultadoJugador(j.getNombre(), puntaje, gano));
        }
        return resultados;
    }

    // ── ACCIÓN: Robo del mazo ─────────────────────────────────────────────────

    @Override
    public boolean agarrarMazo(int indice) throws RemoteException {
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
    public boolean agarrarPozo(int indice) throws RemoteException {
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
    public void bajarJuego(int indice, int[] posicionesAtril) throws RemoteException {
        Jugador jugador = jugadores.get(indice);

        // Obtiene las fichas seleccionadas para validar la jugada.
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
    public void apoyarJuego(int posAtril, int posJuego, int indice, int numJuego) throws RemoteException {
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

        // Convierte la posición recibida al índice utilizado internamente.
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
    public void agregarPozo(int posAtril, int indice) throws RemoteException {
        Jugador jugador = jugadores.get(indice);

        // Verifica que el jugador pueda realizar el descarte.
        ContextoJugada ctxDescarte = contextoBase(indice).build();
        ResultadoValidacion rd = ReglasDeJuego.validarDescarte(ctxDescarte);
        if (!rd.esValido()) {
            fallar(rd.getMensaje(), Eventos.agregarPozo_NO_exitoso);
            return;
        }

        // Obtiene la ficha seleccionada y la retira del atril.
        Ficha ficha;
        try {
            ficha = jugador.verAtril(posAtril);
            jugador.sacarAtril(ficha);
        } catch (Exception e) {
            fallar(e.getMessage(), Eventos.agregarPozo_NO_exitoso);
            return;
        }

        pozo.agregar(ficha);

        // Evalúa las acciones correspondientes cuando el jugador queda sin fichas.
        if (jugador.atrilVacio()) {
            ContextoJugada ctxFinal = contextoConJugador(indice, jugador);
            ResultadoValidacion rf = ReglasDeJuego.validarDescarteFinal(ctxFinal);

            if (!rf.esValido()) {
                // Restaura el estado anterior si la jugada no puede completarse.
                jugador.agregarAtril(pozo.tomar());
                fallar(rf.getMensaje(), Eventos.agregarPozo_NO_exitoso);
                return;
            }

            if (ReglasDeJuego.correspondeTomaMuertoIndirecta(ctxFinal)) {
                gestorMuertos.asignarMuerto(jugador, equipoDe(indice));
                marcarMuertoTomadoEnElEquipo(indice);
                gestorTurnos.avanzarTurno();
                notificarObservadores(Eventos.agregarPozo_exitoso);
                notificarObservadores(Eventos.tomarMuerto_exitoso);
            } else if (ReglasDeJuego.puedeCortar(ctxFinal)) {
                gestorTurnos.finalizarPartida();
                notificarObservadores(Eventos.agregarPozo_exitoso);
                notificarObservadores(Eventos.cortar_exitoso);
                notificarObservadores(Eventos.partida_terminada);
            } else {
                // Si no corresponde tomar el muerto ni finalizar la partida, continúa el siguiente turno.
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

    // ── Metodos auxiliares  privados ──────────────────────────────────────────────────────

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
     * Construye un contexto utilizando el estado actual del jugador después
     * de haberse realizado una acción, para evaluar las reglas aplicables.
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
     * Asigna el muerto al jugador cuando, luego de realizar una jugada,
     * cumple las condiciones establecidas por las reglas del juego.
     */
    private void procesarTomaMuertaDirecta(Jugador jugador, int indice) throws RemoteException {
        ContextoJugada ctx = contextoConJugador(indice, jugador);
        if (ReglasDeJuego.correspondeTomaMuertaDirecta(ctx)) {
            gestorMuertos.asignarMuerto(jugador, equipoDe(indice));
            marcarMuertoTomadoEnElEquipo(indice);
            notificarObservadores(Eventos.tomarMuerto_exitoso);
        }
    }

    /**
     * Registra que el equipo correspondiente ya tomó su muerto para que
     * dicha información sea considerada durante el resto de la partida y
     * en el cálculo del puntaje final.
     */
    private void marcarMuertoTomadoEnElEquipo(int indice) {
        int equipo = equipoDe(indice);
        for (int i = 0; i < jugadores.size(); i++) {
            if (equipoDe(i) == equipo) {
                jugadores.get(i).setYaTomoMuerto(true);
            }
        }
    }

    /**
     * Obtiene las fichas ubicadas en las posiciones indicadas del atril sin
     * modificar su contenido. Se utiliza para validar una jugada antes de
     * ejecutarla.
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

    private boolean fallar(String mensaje, Eventos evento) throws RemoteException {
        ultimoMensajeError = mensaje != null ? mensaje : "Error desconocido.";
        notificarObservadores(evento);
        return false;
    }
}