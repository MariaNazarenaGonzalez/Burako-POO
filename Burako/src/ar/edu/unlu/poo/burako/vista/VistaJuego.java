package ar.edu.unlu.poo.burako.vista;

import ar.edu.unlu.poo.burako.modelo.Eventos;

/**
* Define las operaciones que debe implementar cualquier vista del juego.
*
* Las implementaciones son responsables de representar el estado de la
* partida y reaccionar a los eventos enviados por el controlador.
*/

public interface VistaJuego {
    /** Actualiza todos los elementos visibles de la partida. */
    void mostrarMesa();

    /** Procesa un evento generado durante el desarrollo de la partida. */
    void mesaje(Eventos eventos);
}
