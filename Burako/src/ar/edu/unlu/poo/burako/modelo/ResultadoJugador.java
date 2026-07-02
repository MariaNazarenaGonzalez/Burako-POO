package ar.edu.unlu.poo.burako.modelo;

import java.io.Serializable;

/**
 * Objeto de transferencia de datos (DTO) que representa el resultado final
 * de un jugador al terminar la partida.
 *
 * NUEVO - Extraído para eliminar la construcción de strings de presentación
 * dentro del modelo (Burako.getPuntajesFinales() construía un StringBuilder
 * con saltos de línea, lo cual es responsabilidad de la Vista).
 *
 * El modelo ahora retorna datos estructurados; la Vista decide cómo formatearlos.
 *
 * MODIFICADO (Fase 8 - Preparación RMIMVC): implementa Serializable.
 * Es devuelto por IBurako.getResultados(); cuando IBurako se invoque de
 * forma remota, todo tipo que cruce esa frontera (parámetros y valores de
 * retorno) debe ser serializable para que RMI pueda transportarlo. No
 * implementarlo hoy funciona porque todo ocurre en la misma JVM, pero
 * fallaría en tiempo de ejecución con NotSerializableException apenas se
 * invoque a través de un stub remoto.
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
