package ar.edu.unlu.poo.burako.modelo;

/**
 * Representa los valores que puede tomar una ficha en una partida de Burako.
 *
 * Incluye las fichas numeradas del 1 al 13 y el comodín. Los valores
 * numéricos utilizan el prefijo {@code N} para cumplir con las reglas
 * de nomenclatura de Java, ya que un identificador no puede comenzar
 * con un número.
 */
public enum FichaNumero {
    N1, N2, N3, N4, N5, N6, N7, N8, N9, N10, N11, N12, N13, Comodin;

    /**
     * Obtiene el siguiente valor numérico en la secuencia de fichas.
     *
     * La secuencia es circular, por lo que después del 13 continúa el 1.
     * El comodín no forma parte de esta secuencia y, por lo tanto, no
     * posee un valor siguiente.
     *
     * @return el siguiente número de la secuencia o {@code null} si la
     *         ficha corresponde al comodín.
     */
    public FichaNumero siguiente() {
        FichaNumero[] valores = FichaNumero.values();
        int idx = this.ordinal();
        // El comodín queda fuera de la secuencia numérica y la numeración es circular.
        if (this == Comodin) return null;
        if (idx < valores.length - 2) { // -2 para excluir Comodin
            return valores[idx + 1];
        }
        return N1; // N13 -> N1
    }
}
