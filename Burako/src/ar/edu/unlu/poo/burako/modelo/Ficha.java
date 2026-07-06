package ar.edu.unlu.poo.burako.modelo;

/**
 * Representa una ficha del juego Burako.
 *
 * Cada ficha está compuesta por un color y un número,
 * los cuales determinan su comportamiento dentro de
 * las jugadas y el puntaje que aporta al finalizar
 * la partida.
 *
 * La clase es inmutable: una vez creada una ficha,
 * sus atributos no pueden modificarse.
 */
public class Ficha implements FichaMostrable {

    private static final long serialVersionUID = 1L;

    private final FichaColor color;
    private final FichaNumero numero;
    /**
     * Crea una ficha con el color y número indicados.
     *
     * @param color color de la ficha.
     * @param numero número o valor representado por la ficha.
     */
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
     * Obtiene el valor en puntos de la ficha de acuerdo
     * con las reglas del juego.
     *
     * @return puntaje correspondiente a la ficha.
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
     * Determina si la ficha puede actuar como comodín.
     *
     * En Burako, tanto el comodín tradicional como la
     * ficha número dos pueden cumplir esta función
     * dependiendo del tipo de juego formado.
     *
     * @return {@code true} si la ficha puede utilizarse
     * como comodín; {@code false} en caso contrario.
     */
    public boolean esComodin() {
        return numero == FichaNumero.Comodin || numero == FichaNumero.N2;
    }

    /**
     * Devuelve una representación textual de la ficha.
     *
     * @return cadena con el formato [COLOR_NUMERO].
     */
    @Override
    public String toString() {
        return "[" + color.name() + "_" + numero.name() + "]";
    }
}
