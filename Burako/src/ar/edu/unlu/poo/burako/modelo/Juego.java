package ar.edu.unlu.poo.burako.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contenedor de fichas que forman un juego en la mesa.
 *
 * Responsabilidad única: almacenar las fichas de un juego, mantener su tipo
 * actualizado y calcular su puntaje individual.
 *
 * NO valida reglas de negocio (eso es ReglasDeJuego).
 * NO clasifica por sí mismo (delega a ValidadorFormacion).
 * NO decide si el jugador puede bajar o apoyar (eso es ReglasDeJuego).
 *
 * calcularPuntaje() aplica los bonos de Burako limpio (+200) y sucio (+100)
 * ya que son propiedades del juego en sí, no del jugador.
 *
 * MODIFICADO (Fase 6 - Persistencia): implementa Serializable; las fichas
 * bajadas a la mesa forman parte del estado guardable de una partida
 * (es referenciado desde Jugador).
 */
public class Juego implements JuegoMostrable {

    private static final long serialVersionUID = 1L;

    private final List<Ficha> fichas;
    private TipoJuego         tipo;

    /**
     * Crea un juego a partir de las fichas dadas.
     * ValidadorFormacion ya fue consultado por ReglasDeJuego antes de esta llamada;
     * aquí se vuelve a clasificar para inicializar el tipo interno.
     *
     * @throws Exception si la combinación no es válida (defensa ante uso incorrecto).
     */
    public Juego(List<Ficha> fichas) throws Exception {
        this.fichas = new ArrayList<>(fichas);
        this.tipo   = ValidadorFormacion.clasificar(this.fichas);
    }

    // ── JuegoMostrable ─────────────────────────────────────────────────────────

    @Override
    public List<FichaMostrable> getFichas() {
        return Collections.unmodifiableList(fichas);
    }

    @Override
    public TipoJuego getTipo() {
        return tipo;
    }

    // ── Operaciones internas (invocadas solo por Jugador) ──────────────────────

    /**
     * Agrega una ficha en la posición {@code pos} (1-based).
     * Precondición: ReglasDeJuego.validarApoyarJuego() aprobó la operación.
     * Si una ficha real reemplaza a un comodín en una escalera, el comodín
     * se desplaza al final del juego.
     */
    public void agregar(Ficha ficha, int pos) throws Exception {
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

    /** Copia interna de las fichas (para que CalculadorPuntaje/ReglasDeJuego operen sin cast). */
    public List<Ficha> getFichasInternas() {
        return Collections.unmodifiableList(fichas);
    }

    /** Cantidad de fichas actuales en el juego. */
    public int cantFichas() {
        return fichas.size();
    }

    /**
     * Puntaje de este juego, incluyendo bono de Burako.
     *
     * Burako limpio (canasta pura, 7+ fichas sin comodín negro ni N2 fuera
     * de lugar): +200 puntos extra.
     * Burako sucio (canasta impura, 7+ fichas con comodín): +100 puntos extra.
     * Juegos sin canasta: solo valor de fichas.
     */
    public int calcularPuntaje() {
        int valorFichas = fichas.stream().mapToInt(Ficha::getValor).sum();

        if (ReglasDeJuego.esBurakoLimpio(fichas)) {
            return valorFichas + 200;
        } else if (ReglasDeJuego.esBurakoSucio(fichas)) {
            return valorFichas + 100;
        }
        return valorFichas;
    }
}
