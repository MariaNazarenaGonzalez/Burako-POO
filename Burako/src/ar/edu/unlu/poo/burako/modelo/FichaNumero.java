package ar.edu.unlu.poo.burako.modelo;

/**
 * Números posibles de una ficha de Burako, incluyendo el comodín.
 * Los valores numéricos usan prefijo N para evitar identificadores inválidos.
 */
public enum FichaNumero {
    N1, N2, N3, N4, N5, N6, N7, N8, N9, N10, N11, N12, N13, Comodin;

    /**
     * Retorna el número siguiente en la secuencia (N1 sigue al N13 para escalera circular).
     * El comodín nunca tiene siguiente numérico.
     */
    public FichaNumero siguiente() {
        FichaNumero[] valores = FichaNumero.values();
        int idx = this.ordinal();
        // Comodin no tiene siguiente; N13 tiene siguiente N1 (circularidad del Burako)
        if (this == Comodin) return null;
        if (idx < valores.length - 2) { // -2 para excluir Comodin
            return valores[idx + 1];
        }
        return N1; // N13 -> N1
    }
}