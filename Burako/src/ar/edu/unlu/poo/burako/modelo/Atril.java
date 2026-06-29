package ar.edu.unlu.poo.burako.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa el atril de un jugador: la colección de fichas en su mano.
 *
 * MODIFICADO respecto al original:
 * - Cambiado ArrayList a List en firmas públicas para reducir acoplamiento
 *   con la implementación concreta.
 * - get() retorna List<FichaMostrable> en lugar de ArrayList para no exponer
 *   la implementación subyacente ni permitir modificaciones externas
 *   (se usa unmodifiableList).
 * - Sin importaciones de Swing, AWT ni consola.
 */
public class Atril {

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

    /** Retorna una vista de solo lectura del atril como FichaMostrable. */
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
