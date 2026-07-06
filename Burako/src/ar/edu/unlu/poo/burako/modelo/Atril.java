package ar.edu.unlu.poo.burako.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa el conjunto de fichas que posee un jugador durante la partida.
 * Permite agregar, consultar y remover fichas, además de exponer una vista
 * de solo lectura de su contenido.
 */
public class Atril implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Ficha> fichas;

    public Atril(List<Ficha> fichasIniciales) {
        this.fichas = new ArrayList<>(fichasIniciales);
    }

    /** Agrega una sola ficha al atril. */
    public void agregar(Ficha ficha) {
        fichas.add(ficha);
    }

    /** Agrega una lista de fichas al atril. */
    public void agregar(List<Ficha> nuevas) {
        fichas.addAll(nuevas);
    }

    /**
     * Retorna las fichas en las posiciones indicadas (1-based) sin removerlas.
     * @throws Exception si alguna posición está fuera de rango.
     */
    public List<Ficha> ver(int[] posiciones) throws Exception {
        List<Ficha> resultado = new ArrayList<>();
        for (int p : posiciones) {
            if (p < 1 || p > fichas.size()) {
                throw new Exception("La posición " + p + " no existe en el atril (tamaño: " + fichas.size() + ").");
            }
            resultado.add(fichas.get(p - 1));
        }
        return resultado;
    }

    /**
     * Remueve las fichas indicadas del atril.
     * @throws Exception si alguna ficha no se encuentra.
     */
    public void sacar(List<Ficha> aRemover) throws Exception {
        for (Ficha f : aRemover) {
            if (!fichas.remove(f)) {
                throw new Exception("No se pudo remover la ficha " + f + " del atril.");
            }
        }
    }

    /**
     * Devuelve una vista de solo lectura de las fichas del atril para evitar
     * modificaciones externas.
     */
    public List<FichaMostrable> get() {
        return Collections.unmodifiableList(fichas);
    }

    /** Retorna true si el atril no tiene fichas. */
    public boolean estaVacio() {
        return fichas.isEmpty();
    }

    /** Retorna la cantidad de fichas en el atril. */
    public int cantFichas() {
        return fichas.size();
    }
}
