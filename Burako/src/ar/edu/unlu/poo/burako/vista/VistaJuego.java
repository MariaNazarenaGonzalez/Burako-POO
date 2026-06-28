package ar.edu.unlu.poo.burako.vista;

import ar.edu.unlu.poo.burako.modelo.Eventos;

public interface VistaJuego {
    void mostrarMesa();

    void mesaje(Eventos eventos);

    void mesaje(Exception e);
}
