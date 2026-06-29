package ar.edu.unlu.poo.burako.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa el mazo de fichas de Burako (106 fichas: 4 colores × 13 números × 2 + 2 comodines).
 *
 * MODIFICADO respecto al original:
 * - Eliminados imports de java.awt.Color, java.text.BreakIterator (eran dependencias
 *   de presentación inadvertidas y totalmente innecesarias en el modelo de dominio).
 * - El método fichaColorSig(), que antes vivía en Ficha como método estático de utilidad,
 *   se movió aquí como método privado ya que solo lo usa Mazo para construirse.
 * - Cambiado ArrayList a List en firmas para reducir acoplamiento con la implementación.
 */
public class Mazo {

    private final List<Ficha> fichas;

    public Mazo() {
        fichas = new ArrayList<>();

        FichaColor color = FichaColor.Rojo;
        for (int c = 0; c < 4; c++) {
            for (FichaNumero num : numerosBase()) {
                fichas.add(new Ficha(color, num));
                fichas.add(new Ficha(color, num)); // cada número aparece dos veces por color
            }
            color = siguienteColor(color);
        }
        fichas.add(new Ficha(FichaColor.Negro, FichaNumero.Comodin));
        fichas.add(new Ficha(FichaColor.Negro, FichaNumero.Comodin));

        Collections.shuffle(fichas);
    }

    /**
     * Extrae y retorna las primeras {@code cantidad} fichas del mazo.
     */
    public List<Ficha> sacar(int cantidad) {
        List<Ficha> extraidas = new ArrayList<>(fichas.subList(0, cantidad));
        fichas.subList(0, cantidad).clear();
        return extraidas;
    }

    /**
     * Extrae y retorna la primera ficha del mazo.
     * @return la ficha, o null si el mazo está vacío.
     */
    public Ficha sacarFicha() {
        if (fichas.isEmpty()) return null;
        return fichas.remove(0);
    }

    /** Retorna true si el mazo está vacío. */
    public boolean estaVacio() {
        return fichas.isEmpty();
    }

    // ── Helpers privados ───────────────────────────────────────────────────────

    /** Los 13 números base (sin comodín). */
    private static FichaNumero[] numerosBase() {
        FichaNumero[] todos = FichaNumero.values();
        FichaNumero[] base  = new FichaNumero[todos.length - 1]; // excluye Comodin
        System.arraycopy(todos, 0, base, 0, base.length);
        return base;
    }

    private static FichaColor siguienteColor(FichaColor actual) {
        FichaColor[] colores = FichaColor.values();
        int idx = actual.ordinal();
        return idx < colores.length - 1 ? colores[idx + 1] : colores[0];
    }
}