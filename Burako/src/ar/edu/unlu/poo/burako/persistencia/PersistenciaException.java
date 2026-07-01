package ar.edu.unlu.poo.burako.persistencia;

/**
 * Excepción no chequeada que envuelve cualquier fallo ocurrido al leer o
 * escribir información persistida (errores de E/S, archivo corrupto,
 * clase no encontrada al deserializar, identificador inexistente, etc.).
 *
 * Se usa una RuntimeException para no obligar a Main ni a los futuros
 * llamadores (Controlador, Vistas) a declarar "throws" en cadena por una
 * preocupación que es exclusiva de la capa de persistencia.
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
