package ar.edu.unlu.poo.burako.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa un juego formado por un conjunto de fichas sobre la mesa.
 *
 * Un juego puede corresponder a una escalera, un grupo o una canasta,
 * según la combinación de fichas que lo compongan.
 *
 * Esta clase administra las fichas que pertenecen al juego, mantiene
 * actualizado su tipo y calcula el puntaje obtenido de acuerdo con
 * las reglas del Burako.
 *
 * La validación de las jugadas y la formación de nuevos juegos son
 * responsabilidad de otras clases del modelo.
 */
public class Juego implements JuegoMostrable {

    private static final long serialVersionUID = 1L;

    private final List<Ficha> fichas;
    private TipoJuego         tipo;

   /**
     * Crea un nuevo juego a partir de las fichas indicadas.
     *
     * Durante la construcción se determina automáticamente el tipo
     * de juego correspondiente.
     *
     * @param fichas fichas que formarán el juego.
     * @throws Exception si la combinación de fichas no representa
     *         un juego válido.
     */
    public Juego(List<Ficha> fichas) throws Exception {
        this.fichas = new ArrayList<>(fichas);
        this.tipo   = ValidadorFormacion.clasificar(this.fichas);
    }

    // ── JuegoMostrable ─────────────────────────────────────────────────────────
    /**
     * Obtiene una vista de solo lectura de las fichas que
     * componen el juego.
     *
     * @return lista inmodificable de fichasMostrables.
     */
    @Override
    public List<FichaMostrable> getFichas() {
        return Collections.unmodifiableList(fichas);
    }
    /**
     * Obtiene el tipo actual del juego.
     *
     * @return tipo del juego.
     */
    @Override
    public TipoJuego getTipo() {
        return tipo;
    }

    // ── Operaciones internas (invocadas solo por Jugador) ──────────────────────

    /**
     * Agrega una ficha al juego en la posición indicada.
     *
     * Si la inserción produce una canasta, el tipo del juego
     * se actualiza automáticamente. En escaleras, cuando una
     * ficha reemplaza a un comodín, este se desplaza al final
     * del juego según las reglas del Burako.
     *
     * @param ficha ficha que se agregará.
     * @param pos posición de inserción (comenzando en 1).
     * @throws Exception si la posición indicada es inválida.
     */
    void agregar(Ficha ficha, int pos) throws Exception {
        if (pos < 1 || pos > fichas.size() + 1) {
            throw new Exception("Posición " + pos + " fuera de rango (el juego tiene "
                    + fichas.size() + " fichas).");
        }

        int idx = pos - 1;

        boolean esEscaleraDestino =
                tipo == TipoJuego.Escalera
                || tipo == TipoJuego.CanastaImpuraEscalera
                || tipo == TipoJuego.CanastaPuraEscalera;

        boolean desplazaComodin = esEscaleraDestino
                && idx < fichas.size()
                && fichas.get(idx).esComodin()
                && !ficha.esComodin();
        // Si una ficha reemplaza un comodín en una escalera,
        // el comodín pasa al final del juego.
        if (desplazaComodin) {
            Ficha comodinDesplazado = fichas.remove(idx);
            fichas.add(idx, ficha);
            fichas.add(comodinDesplazado);
        } else {
            fichas.add(idx, ficha);
        }

        if (fichas.size() >= 7) {
            tipo = ValidadorFormacion.reclasificarComoCanasta(fichas);
        }
    }

    /**
     * Obtiene una vista inmodificable de las fichas del juego.
     *
     * Este método es utilizado internamente por el modelo.
     *
     * @return lista de fichas del juego.
     */
    List<Ficha> getFichasInternas() {
        return Collections.unmodifiableList(fichas);
    }

    /**
     * Obtiene la cantidad de fichas que forman el juego.
     *
     * @return cantidad de fichas.
     */
    int cantFichas() {
        return fichas.size();
    }

    /**
     * Calcula el puntaje total del juego.
     *
     * El resultado corresponde a la suma del valor de todas
     * las fichas y, cuando corresponde, incluye la bonificación
     * otorgada por formar una canasta limpia o una canasta sucia.
     *
     * @return puntaje total del juego.
     */
    int calcularPuntaje() {
        int valorFichas = fichas.stream().mapToInt(Ficha::getValor).sum();

        if (ReglasDeJuego.esBurakoLimpio(fichas)) {
            return valorFichas + 200;
        } else if (ReglasDeJuego.esBurakoSucio(fichas)) {
            return valorFichas + 100;
        }
        return valorFichas;
    }
}
