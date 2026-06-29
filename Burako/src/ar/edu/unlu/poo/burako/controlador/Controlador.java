package ar.edu.unlu.poo.burako.controlador;

import ar.edu.unlu.poo.burako.modelo.*;
import ar.edu.unlu.poo.burako.vista.VistaJuego;

import java.util.List;

/**
 * Controlador: intermediario entre Modelo y Vista.
 *
 * MODIFICADO (Fase 4):
 * - El campo y constructor ahora dependen de IBurako (no de Burako concreto).
 *   Principio de Inversión de Dependencias: el controlador desconoce la implementación.
 * - Se eliminó notificar(Eventos, Exception): corresponde a la firma antigua.
 *   Los mensajes de error se obtienen con burako.getUltimoMensajeError().
 * - Se eliminaron getJuegos(), getAtril(), getNombre(), cantJuegos() que accedían
 *   a burako.getJugador() — método concreto ausente en IBurako.
 *   Reemplazados por llamadas directas a los métodos de IBurako.
 *
 * Invariantes:
 * - El controlador NO contiene reglas del juego.
 * - El controlador NO conoce implementaciones concretas del modelo.
 * - Toda actualización de la vista ocurre desde aquí, al recibir un evento.
 */
public class Controlador implements Observador {

    private final IBurako burako;
    private VistaJuego vista;

    public Controlador(IBurako burako) {
        this.burako = burako;
    }

    // ── Observer ──────────────────────────────────────────────────────────────

    @Override
    public void notificar(Eventos evento) {
        vista.mostrarMesa();
        vista.mesaje(evento);
    }

    // ── Configuración ─────────────────────────────────────────────────────────

    public void setVista(VistaJuego vista) {
        this.vista = vista;
    }

    public void setNombres(String nombreJugador1, String nombreJugador2) {
        burako.setNombres(nombreJugador1, nombreJugador2);
    }

    // ── Consultas de estado ────────────────────────────────────────────────────

    public int getTurnoActual() {
        return burako.getTurnoActual();
    }

    public EstadoTurno getEstadoTurno() {
        return burako.getEstadoTurno();
    }

    public boolean puedeTomar(int turno) {
        return burako.puedeTomar(turno);
    }

    public boolean puedeJugar(int turno) {
        return burako.puedeJugar(turno);
    }

    public String getNombre(int turno) {
        return burako.getNombreJugador(turno);
    }

    public String getUltimoMensajeError() {
        return burako.getUltimoMensajeError();
    }

    // ── Consultas de datos de juego ────────────────────────────────────────────

    public List<JuegoMostrable> getJuegos(int jugador) {
        return burako.getJuegos(jugador);
    }

    public List<FichaMostrable> getPozo() {
        return burako.getPozo();
    }

    public List<FichaMostrable> getAtril(int turno) {
        return burako.getAtril(turno);
    }

    public int cantJuegos(int turno) {
        return burako.cantJuegos(turno);
    }

    public List<ResultadoJugador> getResultados() {
        return burako.getResultados();
    }

    // ── Acciones del turno ─────────────────────────────────────────────────────

    public boolean agarrarPozo(int turno) {
        return burako.agarrarPozo(turno);
    }

    public boolean agarrarMazo(int turno) {
        return burako.agarrarMazo(turno);
    }

    public void bajarJuego(int turno, int[] listafichas) {
        burako.bajarJuego(turno, listafichas);
    }

    public void agregarPozo(int n, int turno) {
        burako.agregarPozo(n, turno);
    }

    public void apoyarJuego(int ficha, int pos, int turno, int juego) {
        burako.apoyarJuego(ficha, pos, turno, juego);
    }
}
