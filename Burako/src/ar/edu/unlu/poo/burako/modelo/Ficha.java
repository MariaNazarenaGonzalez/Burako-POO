package ar.edu.unlu.poo.burako.modelo;

/**
 * Representa una ficha individual de Burako.
 *
 * MODIFICADO respecto al original:
 * - Eliminados los métodos estáticos fichaNumSig() y fichaColorSig(): la lógica
 *   de "siguiente número" ahora vive en FichaNumero.siguiente(), cohesionando
 *   el comportamiento con su tipo. La lógica de "siguiente color" era solo
 *   usada por Mazo para construirse; se movió allí como método privado.
 * - Actualizado para usar FichaColor y FichaNumero (nombres corregidos).
 * - Ningún import de AWT, Swing ni consola.
 */
public class Ficha implements FichaMostrable {

    private final FichaColor color;
    private final FichaNumero numero;

    public Ficha(FichaColor color, FichaNumero numero) {
        this.color = color;
        this.numero = numero;
    }

    @Override
    public FichaColor getColor() {
        return color;
    }

    @Override
    public FichaNumero getNum() {
        return numero;
    }

    /**
     * Retorna el valor en puntos de esta ficha según las reglas de Burako.
     */
    public int getValor() {
        switch (numero) {
            case N1:      return 15;
            case N2:      return 20;
            case Comodin: return 50;
            case N3:
            case N4:
            case N5:
            case N6:
            case N7:      return 5;
            default:      return 10; // N8 a N13
        }
    }

    /**
     * Indica si esta ficha actúa como comodín (comodín negro o el 2 en ciertos contextos).
     * El 2 puede actuar como comodín en escaleras e influye en la pureza de las canastas.
     */
    public boolean esComodin() {
        return numero == FichaNumero.Comodin || numero == FichaNumero.N2;
    }

    @Override
    public String toString() {
        return "[" + color.name() + "_" + numero.name() + "]";
    }
}
