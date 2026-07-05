package ar.edu.unlu.poo.burako.modelo;

import ar.edu.unlu.rmimvc.observer.ObservableRemoto;

import java.io.Serializable;
import java.rmi.RemoteException;
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
 *
 * MODIFICADO (Fase 6 - Persistencia):
 * - Implementa Serializable para permitir guardar/recuperar el estado
 *   completo de una partida usando Serialización Java (ver paquete
 *   persistencia). No se altera ningún comportamiento ni regla del juego.
 *
 * MODIFICADO (Fase 10 - Soporte 2 o 4 jugadores):
 * - Nuevo constructor Burako(int cantidadJugadores) admite 2 o 4. El
 *   constructor sin argumentos se conserva EXACTAMENTE igual (this(2)),
 *   por lo que todo el código y los 55 tests existentes que ya usaban
 *   "new Burako()" siguen funcionando sin cambios.
 * - Con 4 jugadores se juega en 2 equipos de 2 (jugadores 0 y 2 forman un
 *   equipo; 1 y 3 forman el otro — "sentados cruzados", orden de turno
 *   0,1,2,3 sin alterar GestorTurnos, que ya era genérico). Los equipos
 *   comparten ÚNICAMENTE el muerto: cada equipo tiene UN muerto (no uno
 *   por jugador), y si un integrante lo toma, también se marca a su
 *   compañero como "ya tomó muerto" a efectos de puntaje, ya que
 *   físicamente es un solo muerto compartido. Todo lo demás (atril,
 *   juegos bajados, puntaje individual) sigue siendo exclusivo de cada
 *   jugador ("el resto es individual").
 * - Con 4 jugadores se reparten 11 fichas de mano (en vez de 12) para no
 *   agotar el mazo: 4×11 + 2×11(muertos) = 66 de 106 fichas repartidas.
 */
public class Burako extends ObservableRemoto implements IBurako, Serializable {

    private static final long serialVersionUID = 2L;

    private final List<Jugador> jugadores;
    private final Mazo          mazo;
    private final Pozo          pozo;
    private final GestorTurnos  gestorTurnos;
    private final GestorMuertos gestorMuertos;

    private String ultimoMensajeError = "";

    /** Partida de 2 jugadores (comportamiento original, sin cambios). */
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
     * Retorna el índice de equipo (0 o 1) del jugador dado. Con 2 jugadores,
     * cada uno es su propio equipo (0 y 1 respectivamente): el
     * comportamiento de toma de muerto queda idéntico al de fases
     * anteriores. Con 4 jugadores, 0 y 2 comparten equipo 0; 1 y 3
     * comparten equipo 1.
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
    public void agregarPozo(int posAtril, int indice) throws RemoteException {
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
    private void procesarTomaMuertaDirecta(Jugador jugador, int indice) throws RemoteException {
        ContextoJugada ctx = contextoConJugador(indice, jugador);
        if (ReglasDeJuego.correspondeTomaMuertaDirecta(ctx)) {
            gestorMuertos.asignarMuerto(jugador, equipoDe(indice));
            marcarMuertoTomadoEnElEquipo(indice);
            notificarObservadores(Eventos.tomarMuerto_exitoso);
        }
    }

    /**
     * Marca a TODOS los jugadores del mismo equipo que {@code indice} como
     * "ya tomó muerto" a efectos de puntaje (ReglasDeJuego.calcularPuntaje),
     * ya que el muerto es un recurso compartido por el equipo: si un
     * integrante lo tomó, ningún compañero puede tomar otro (solo hay uno
     * por equipo) y ambos se benefician/perjudican igual en el puntaje.
     * Con 2 jugadores, cada equipo tiene un único integrante, por lo que
     * este método solo marca al propio jugador (comportamiento idéntico al
     * de fases anteriores).
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

    private boolean fallar(String mensaje, Eventos evento) throws RemoteException {
        ultimoMensajeError = mensaje != null ? mensaje : "Error desconocido.";
        notificarObservadores(evento);
        return false;
    }
}