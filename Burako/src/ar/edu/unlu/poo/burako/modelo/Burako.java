package ar.edu.unlu.poo.burako.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación principal del modelo de Burako.
 * Orquesta los colaboradores del dominio e implementa IBurako.
 *
 * MODIFICADO respecto al original:
 *
 * 1. Implementa IBurako (nueva interfaz) en lugar de solo Observado.
 *    El Controlador ahora depende de IBurako, no de Burako directamente (DIP).
 *
 * 2. La lógica de turno fue extraída a GestorTurnos:
 *    - validarTurno(), validarEstado(), avanzarTurno(), puedeTomar(), puedeJugar()
 *    ya no existen en esta clase.
 *
 * 3. La lógica de muertos fue extraída a GestorMuertos:
 *    - El bloque if (!jugador.yaTomoMuerto() && !muertos.isEmpty()) { ... }
 *    que se duplicaba en agregarPozo(), bajarJuego() y apoyarJuego() fue
 *    reemplazado por gestorMuertos.intentarAsignarMuerto(jugador).
 *
 * 4. getPuntajesFinales() fue reemplazado por getResultados() que retorna
 *    List<ResultadoJugador> en lugar de un String formateado.
 *    La Vista decide cómo mostrar los resultados (SRP / MVC).
 *
 * 5. getUltimoMensajeError() reemplaza el transporte de Exception como
 *    parámetro del Observer. Cuando una acción falla, Burako guarda el mensaje
 *    y notifica el evento _NO_exitoso. El Controlador consulta el mensaje y
 *    lo pasa a la Vista sin exponer tipos de excepción del modelo.
 *
 * 6. getJugador() sigue siendo public para compatibilidad con los tests
 *    existentes que acceden directamente a Jugador.
 */
public class Burako implements IBurako {

    // ── Colaboradores del dominio ──────────────────────────────────────────────
    private final List<Jugador>    jugadores;
    private final Mazo             mazo;
    private final Pozo             pozo;
    private final GestorTurnos     gestorTurnos;
    private final GestorMuertos    gestorMuertos;

    // ── Estado del Observer ────────────────────────────────────────────────────
    private final List<Observador> observadores = new ArrayList<>();
    private String                 ultimoMensajeError = "";

    public Burako() {
        mazo = new Mazo();
        pozo = new Pozo();

        // Preparar muertos (11 fichas cada uno) antes de repartir
        List<Ficha> fichasMuerto1 = mazo.sacar(11);
        List<Ficha> fichasMuerto2 = mazo.sacar(11);
        gestorMuertos = new GestorMuertos(fichasMuerto1, fichasMuerto2);

        // Repartir 12 fichas a cada jugador
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
        for (Observador obs : observadores) {
            obs.notificar(evento);
        }
    }

    // ── Configuración ─────────────────────────────────────────────────────────

    @Override
    public void setNombres(String nombre1, String nombre2) {
        jugadores.get(0).setNombre(nombre1);
        jugadores.get(1).setNombre(nombre2);
    }

    // ── Consultas de estado ───────────────────────────────────────────────────

    @Override
    public int getTurnoActual() {
        return gestorTurnos.getTurnoActual();
    }

    @Override
    public EstadoTurno getEstadoTurno() {
        return gestorTurnos.getEstado();
    }

    @Override
    public boolean puedeTomar(int indice) {
        return gestorTurnos.puedeTomar(indice);
    }

    @Override
    public boolean puedeJugar(int indice) {
        return gestorTurnos.puedeJugar(indice);
    }

    @Override
    public String getNombreJugador(int indice) {
        return jugadores.get(indice).getNombre();
    }

    @Override
    public String getUltimoMensajeError() {
        return ultimoMensajeError;
    }

    // ── Consultas de datos ────────────────────────────────────────────────────

    @Override
    public List<FichaMostrable> getPozo() {
        return pozo.get();
    }

    @Override
    public List<FichaMostrable> getAtril(int indice) {
        return jugadores.get(indice).getAtril();
    }

    @Override
    public List<JuegoMostrable> getJuegos(int indice) {
        return jugadores.get(indice).getJugadas();
    }

    @Override
    public int cantJuegos(int indice) {
        return jugadores.get(indice).cantJuegos();
    }

    @Override
    public List<ResultadoJugador> getResultados() {
        int turnoActual = gestorTurnos.getTurnoActual();
        List<ResultadoJugador> resultados = new ArrayList<>();
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j     = jugadores.get(i);
            boolean corto = (i == turnoActual);
            int puntaje   = j.calcularPuntaje(corto);
            // El ganador es quien cortó O quien tiene más puntos al finalizar
            resultados.add(new ResultadoJugador(j.getNombre(), puntaje, corto));
        }
        return resultados;
    }

    // ── Acciones del turno ────────────────────────────────────────────────────

    @Override
    public boolean agarrarMazo(int indice) {
        try {
            gestorTurnos.validarEsTurno(indice);
            gestorTurnos.validarEstado(EstadoTurno.TOMAR, "Debes terminar tu jugada antes de tomar del mazo.");
            Ficha ficha = mazo.sacarFicha();
            if (ficha == null) {
                throw new Exception("El mazo está vacío.");
            }
            jugadores.get(indice).agregarAtril(ficha);
            gestorTurnos.marcarFichaTomada();
            notificarObservadores(Eventos.tomarMazo_exitoso);
            return true;
        } catch (Exception e) {
            registrarError(e);
            notificarObservadores(Eventos.tomarMazo_NO_exitoso);
            return false;
        }
    }

    @Override
    public boolean agarrarPozo(int indice) {
        try {
            gestorTurnos.validarEsTurno(indice);
            gestorTurnos.validarEstado(EstadoTurno.TOMAR, "No puedes tomar el pozo en este momento.");
            if (pozo.estaVacio()) {
                throw new Exception("El pozo está vacío.");
            }
            jugadores.get(indice).agregarAtril(pozo.tomar());
            gestorTurnos.marcarFichaTomada();
            notificarObservadores(Eventos.tomarPozo_exitoso);
            return true;
        } catch (Exception e) {
            registrarError(e);
            notificarObservadores(Eventos.tomarPozo_NO_exitoso);
            return false;
        }
    }

    @Override
    public void bajarJuego(int indice, int[] posicionesAtril) {
        try {
            gestorTurnos.validarEsTurno(indice);
            gestorTurnos.validarEstado(EstadoTurno.JUGAR, "Debes tomar una ficha antes de bajar un juego.");
            Jugador jugador = jugadores.get(indice);
            jugador.bajarJuego(posicionesAtril);
            verificarTomaMuerto(jugador);
            notificarObservadores(Eventos.bajarJuego_exitoso);
        } catch (Exception e) {
            registrarError(e);
            notificarObservadores(Eventos.bajarJuego_NO_exitoso);
        }
    }

    @Override
    public void apoyarJuego(int posAtril, int posJuego, int indice, int numJuego) {
        try {
            gestorTurnos.validarEsTurno(indice);
            gestorTurnos.validarEstado(EstadoTurno.JUGAR, "Debes tomar una ficha antes de apoyar.");
            Jugador jugador = jugadores.get(indice);
            jugador.apoyarJuego(posAtril, posJuego, numJuego);
            verificarTomaMuerto(jugador);
            notificarObservadores(Eventos.apoyarJuego_exitoso);
        } catch (Exception e) {
            registrarError(e);
            notificarObservadores(Eventos.apoyarJuego_NO_exitoso);
        }
    }

    @Override
    public void agregarPozo(int posAtril, int indice) {
        try {
            gestorTurnos.validarEsTurno(indice);
            gestorTurnos.validarEstado(EstadoTurno.JUGAR, "Debes tomar una ficha antes de descartar al pozo.");
            Jugador jugador = jugadores.get(indice);
            Ficha ficha = jugador.verAtril(posAtril);
            jugador.sacarAtril(ficha);
            pozo.agregar(ficha);

            if (jugador.atrilVacio()) {
                if (!jugador.yaTomoMuerto() && gestorMuertos.hayMuertosDisponibles()) {
                    // Toma indirecta del muerto: el turno SÍ avanza
                    gestorMuertos.intentarAsignarMuerto(jugador);
                    notificarObservadores(Eventos.tomarMuerto_exitoso);
                    gestorTurnos.avanzarTurno();
                } else if (jugador.yaTomoMuerto() && jugador.tieneCanasta()) {
                    // Corte exitoso
                    gestorTurnos.finalizarPartida();
                    notificarObservadores(Eventos.cortar_exitoso);
                    notificarObservadores(Eventos.partida_terminada);
                } else {
                    // No puede quedarse sin fichas sin cumplir condiciones de corte
                    throw new Exception("No puedes descartar tu última ficha sin tener al menos una canasta.");
                }
            } else {
                gestorTurnos.avanzarTurno();
            }

            notificarObservadores(Eventos.agregarPozo_exitoso);
        } catch (Exception e) {
            registrarError(e);
            notificarObservadores(Eventos.agregarPozo_NO_exitoso);
        }
    }

    // ── Acceso directo a Jugador (para compatibilidad con tests) ──────────────

    /**
     * Retorna el jugador en el índice indicado.
     * Usado por los tests existentes; el Controlador no debe usar este método.
     */
    public Jugador getJugador(int indice) {
        return jugadores.get(indice);
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    /**
     * Si el jugador quedó sin fichas tras bajar/apoyar, intenta asignarle el muerto
     * (toma directa). El turno NO avanza en toma directa.
     */
    private void verificarTomaMuerto(Jugador jugador) {
        if (jugador.atrilVacio()) {
            boolean asignado = gestorMuertos.intentarAsignarMuerto(jugador);
            if (asignado) {
                notificarObservadores(Eventos.tomarMuerto_exitoso);
            }
        }
    }

    /** Guarda el mensaje de la excepción para que el Controlador lo consulte. */
    private void registrarError(Exception e) {
        ultimoMensajeError = e.getMessage() != null ? e.getMessage() : "Error desconocido.";
    }
}
