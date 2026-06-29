package ar.edu.unlu.poo.burako.modelo;

/**
 * Objeto de transferencia de datos (DTO) que representa el resultado final
 * de un jugador al terminar la partida.
 *
 * NUEVO - Extraído para eliminar la construcción de strings de presentación
 * dentro del modelo (Burako.getPuntajesFinales() construía un StringBuilder
 * con saltos de línea, lo cual es responsabilidad de la Vista).
 *
 * El modelo ahora retorna datos estructurados; la Vista decide cómo formatearlos.
 */
public final class ResultadoJugador {
    private final String nombre;
    private final int puntaje;
    private final boolean esGanador;

    public ResultadoJugador(String nombre, int puntaje, boolean esGanador) {
        this.nombre = nombre;
        this.puntaje = puntaje;
        this.esGanador = esGanador;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public boolean esGanador() {
        return esGanador;
    }
}