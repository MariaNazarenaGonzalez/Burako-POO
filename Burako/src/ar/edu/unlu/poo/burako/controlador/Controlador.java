package ar.edu.unlu.poo.burako.controlador;

import ar.edu.unlu.poo.burako.modelo.*;
import ar.edu.unlu.poo.burako.vista.VistaJuego;

import java.util.ArrayList;

public class Controlador implements Observador {
    Burako burako;
    VistaJuego vista;

    public Controlador(Burako burako) {
        this.burako=burako;
    }

    @Override
    public void notificar(Eventos eventos) {
            vista.mostrarMesa();
            vista.mesaje(eventos);

    }

    @Override
    public void notificar(Eventos eventos, Exception e) {
        vista.mesaje(eventos);
        vista.mesaje(e);
    }

    public void setVista(VistaJuego vista) {
        this.vista=vista;
    }

    public void setNombres(String nombreJugador1, String nombreJugador2) {
        this.burako.setNombres(nombreJugador1,nombreJugador2);
    }

    public ArrayList<JuegoMostrable> getJuegos(int jugador) {
        return this.burako.getJugador(jugador).getJugadas();
    }

    public ArrayList<FichaMostrable> getPozo() {
        return this.burako.getPozo();
    }

    public ArrayList<FichaMostrable> getAtril(int turno) {
        return this.burako.getJugador(turno).getAtril();
    }

    public boolean agarrarPozo(int turno) {
        return this.burako.agarrarPozo(turno);
    }

    public boolean agarrarMazo(int turno) {
        return this.burako.agarrarMazo(turno);
    }

    public void bajarJuego(int turno, int[] listafichas) {
        this.burako.bajarJuego(turno, listafichas);
    }

    public void agregarPozo(int n,int turno) {
        this.burako.agregarPozo(n,turno);
    }

    public void apoyarJuego(int ficha, int pos,int turno,int juego) {
        this.burako.apoyarJuego(ficha,pos,turno,juego);
    }

    public String getNombre(int turno) {
        return this.burako.getJugador(turno).getNombre();
    }

    public int cantJuegos(int turno) {
        return this.burako.getJugador(turno).cantJuegos();
    }

    public int getTurnoActual() {
        return this.burako.getTurnoActual();
    }

    public boolean puedeTomar(int turno) {
        return this.burako.puedeTomar(turno);
    }

    public boolean puedeJugar(int turno) {
        return this.burako.puedeJugar(turno);
    }

    public EstadoTurno getEstadoTurno() {
        return this.burako.getEstadoTurno();
    }
}
