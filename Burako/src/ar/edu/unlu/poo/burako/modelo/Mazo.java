package ar.edu.unlu.poo.burako.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa el mazo de fichas utilizado durante una partida de Burako.
 *
 * El mazo está compuesto por todas las fichas del juego, incluyendo
 * los comodines, y se inicializa automáticamente en orden aleatorio.
 *
 * Proporciona las operaciones necesarias para extraer fichas y
 * consultar si aún quedan elementos disponibles.
 */
public class Mazo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Ficha> fichas;
    /**
     * Crea un mazo completo de Burako y mezcla sus fichas
     * de forma aleatoria.
     */
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
     * Extrae la cantidad indicada de fichas desde el comienzo
     * del mazo.
     *
     * @param cantidad cantidad de fichas a retirar.
     * @return lista con las fichas extraídas.
     */
    public List<Ficha> sacar(int cantidad) {
        List<Ficha> extraidas = new ArrayList<>(fichas.subList(0, cantidad));
        fichas.subList(0, cantidad).clear();
        return extraidas;
    }

    /**
     * Extrae la primera ficha disponible del mazo.
     *
     * @return ficha extraída o {@code null} si el mazo está vacío.
     */
    public Ficha sacarFicha() {
        if (fichas.isEmpty()) return null;
        return fichas.remove(0);
    }

    /** Retorna true si el mazo está vacío. */
    public boolean estaVacio() {
        return fichas.isEmpty();
    }

    // ── Metodos Auxiliates privados ───────────────────────────────────────────────────────

    /**
     * Obtiene los valores numéricos utilizados para construir
     * el mazo, excluyendo el comodín.
     *
     * @return arreglo con los números base.
     */
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
