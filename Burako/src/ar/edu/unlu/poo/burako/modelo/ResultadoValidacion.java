package ar.edu.unlu.poo.burako.modelo;

/**
 * Objeto de valor que encapsula el resultado de una validación de regla.
 *
 * Reemplaza el uso de Exception como mecanismo de flujo de control.
 * Cada método de ReglasDeJuego retorna un ResultadoValidacion en lugar
 * de lanzar excepciones para casos de negocio esperados (turno incorrecto,
 * ficha inválida, etc.). Las excepciones quedan para errores de programación.
 *
 * Uso:
 *   ResultadoValidacion r = reglas.validarRoboMazo(turno, indiceJugador);
 *   if (!r.esValido()) {
 *       // r.getMensaje() describe el motivo del rechazo
 *   }
 */
public final class ResultadoValidacion {

    private final boolean valido;
    private final String  mensaje;

    private ResultadoValidacion(boolean valido, String mensaje) {
        this.valido  = valido;
        this.mensaje = mensaje;
    }

    /** Crea un resultado de validación exitosa. */
    public static ResultadoValidacion ok() {
        return new ResultadoValidacion(true, "");
    }

    /** Crea un resultado de validación fallida con un motivo. */
    public static ResultadoValidacion fallo(String mensaje) {
        return new ResultadoValidacion(false, mensaje);
    }

    /** Retorna true si la acción cumple las reglas. */
    public boolean esValido() {
        return valido;
    }

    /**
     * Retorna el mensaje de error cuando esValido() == false.
     * Retorna cadena vacía cuando esValido() == true.
     */
    public String getMensaje() {
        return mensaje;
    }
}
