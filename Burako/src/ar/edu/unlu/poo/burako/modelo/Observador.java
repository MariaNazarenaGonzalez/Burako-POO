package ar.edu.unlu.poo.burako.modelo;

/**
 * Contrato del observador en el patrón Observer del modelo.
 *
 * MODIFICADO respecto al original:
 * - Se eliminó el segundo método notificar(Eventos, Exception).
 *   Transportar objetos Exception entre capas acopla el modelo con el manejo
 *   de errores de la vista, y en RMI las excepciones arbitrarias no son
 *   serializables de forma confiable.
 * - Los errores ahora se comunican con eventos específicos (ej: tomarMazo_NO_exitoso).
 *   El mensaje de error se obtiene mediante getUltimoMensajeError() en IBurako,
 *   lo que mantiene el flujo limpio sin transportar excepciones.
 */
public interface Observador {
    /**
     * Notifica al observador que ocurrió un evento en el modelo.
     *
     * @param evento el evento ocurrido
     */
    public void notificar(Eventos evento);
}
