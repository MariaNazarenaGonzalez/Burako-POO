package ar.edu.unlu.poo.burako.vista;

import ar.edu.unlu.poo.burako.modelo.Eventos;

/**
 * Contrato de todas las vistas del juego.
 *
 * Las vistas solo muestran información; no contienen lógica de negocio.
 * La actualización siempre ocurre a través del Controlador.
 *
 * MODIFICADO (Fase 4):
 * - Se eliminó mesaje(Exception e). La firma antigua transportaba excepciones
 *   directamente a la vista acoplando capas. Los errores ahora se obtienen
 *   mediante IBurako.getUltimoMensajeError() desde el Controlador.
 */
public interface VistaJuego {
    /** Refresca la representación completa de la mesa. */
    void mostrarMesa();

    /** Muestra el resultado de un evento del modelo. */
    void mesaje(Eventos eventos);
}
