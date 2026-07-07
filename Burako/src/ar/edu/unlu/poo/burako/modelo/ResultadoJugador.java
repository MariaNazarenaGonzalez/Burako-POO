package ar.edu.unlu.poo.burako.modelo;

import java.io.Serializable;

/**
 * Representa el resultado obtenido por un jugador al finalizar la partida.
 *
 * Contiene el nombre del jugador, el puntaje alcanzado y un indicador
 * que señala si pertenece al equipo ganador. Esta información se utiliza
 * para comunicar los resultados finales entre las distintas capas de la
 * aplicación.
 */
public final class ResultadoJugador implements Serializable {

    private static final long serialVersionUID = 1L;

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
