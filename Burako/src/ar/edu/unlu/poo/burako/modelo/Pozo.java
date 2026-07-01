package ar.edu.unlu.poo.burako.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa el pozo de descarte de Burako.
 * Las fichas descartadas por los jugadores se acumulan aquí.
 * Tomar el pozo retira TODAS las fichas que contiene.
 *
 * MODIFICADO respecto al original:
 * - Cambiado ArrayList a List en firmas públicas.
 * - get() retorna vista no modificable (unmodifiableList).
 * - estaVacio() agregado para evitar consultar get().isEmpty() en el modelo.
 * - (Fase 6) Implementa Serializable: forma parte del estado guardable
 *   de una partida (es referenciado desde Burako).
 */
public class Pozo implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Ficha> fichas;

    public Pozo() {
        fichas = new ArrayList<>();
    }

    /**
     * Agrega una ficha al pozo.
     */
    public void agregar(Ficha ficha) {
        fichas.add(ficha);
    }

    /**
     * Retira todas las fichas del pozo y las retorna.
     * Después de esta operación el pozo queda vacío.
     */
    public List<Ficha> tomar() {
        List<Ficha> tomadas = fichas;
        fichas = new ArrayList<>();
        return tomadas;
    }

    /**
     * Retorna una vista de solo lectura del pozo como FichaMostrable.
     */
    public List<FichaMostrable> get() {
        return Collections.unmodifiableList(fichas);
    }

    /** Retorna true si el pozo no tiene fichas. */
    public boolean estaVacio() {
        return fichas.isEmpty();
    }
}
