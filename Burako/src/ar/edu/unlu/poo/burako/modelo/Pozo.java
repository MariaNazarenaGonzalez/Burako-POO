package ar.edu.unlu.poo.burako.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa el pozo de descarte de la partida.
 *
 * Todas las fichas descartadas por los jugadores se almacenan
 * en este contenedor hasta que algún jugador decide tomar el
 * pozo completo.
 */
public class Pozo implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Ficha> fichas;
    /**
     * Crea un pozo inicialmente vacío.
     */
    public Pozo() {
        fichas = new ArrayList<>();
    }

    /**
     * Agrega una ficha al pozo.
     *
     * @param ficha ficha que será descartada.
     */
    public void agregar(Ficha ficha) {
        fichas.add(ficha);
    }

    /**
     * Retira todas las fichas del pozo.
     *
     * Después de esta operación el pozo queda vacío.
     *
     * @return fichas que contenía el pozo.
     */
    public List<Ficha> tomar() {
        List<Ficha> tomadas = fichas;
        fichas = new ArrayList<>();
        return tomadas;
    }

    /**
     * Obtiene una vista de solo lectura de las fichas
     * almacenadas en el pozo.
     *
     * @return lista inmodificable de fichas mostrables.
     */
    public List<FichaMostrable> get() {
        return Collections.unmodifiableList(fichas);
    }

    /** Retorna true si el pozo no tiene fichas. */
    public boolean estaVacio() {
        return fichas.isEmpty();
    }
}
