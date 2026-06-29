package ar.edu.unlu.poo.burako.modelo;

/**
 * Contrato del sujeto observado en el patrón Observer.
 *
 * MODIFICADO respecto al original:
 * - Se corrigió el typo "obsevador" → "observador".
 * - Se declaró el método de registro como parte del contrato público de la interfaz
 *   (en el original faltaba el método de notificación con error en la interfaz,
 *   aunque Burako lo implementaba).
 * - La notificación de errores se unifica en un solo canal usando solo Eventos,
 *   sin transportar Exception como parámetro.
 */
public interface Observado {
    /**
     * Registra un observador que recibirá notificaciones de cambios de estado.
     */
    void agregarObservador(Observador observador);

    /**
     * Notifica a todos los observadores registrados de un evento.
     */
    void notificarObservadores(Eventos evento);
}
