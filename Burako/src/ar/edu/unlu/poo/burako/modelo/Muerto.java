package ar.edu.unlu.poo.burako.modelo;

import java.io.Serializable;
import java.util.List;

/**
 * Representa el muerto utilizado durante una partida de Burako.
 *
 * Un muerto es un conjunto de fichas reservado para un equipo,
 * que puede incorporarse al juego cuando se cumplen las
 * condiciones establecidas por el reglamento.
 */
public class Muerto implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Ficha> fichas;
    private boolean tomado = false;
    /**
     * Crea un muerto con las fichas indicadas.
     *
     * @param fichas fichas que conforman el muerto.
     */
    public Muerto(List<Ficha> fichas) {
        this.fichas = fichas;
    }

    /**
     * Entrega las fichas del muerto y lo marca como utilizado.
     *
     * Un muerto solo puede ser tomado una única vez.
     *
     * @return fichas que forman el muerto.
     * @throws IllegalStateException si el muerto ya fue utilizado.
     */
    public List<Ficha> tomar() {
        if (tomado) {
            throw new IllegalStateException("Este muerto ya fue tomado.");
        }
        tomado = true;
        return fichas;
    }

    /** Retorna true si este muerto aún no fue tomado. */
    public boolean estaDisponible() {
        return !tomado;
    }
}
