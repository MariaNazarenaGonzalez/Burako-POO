package ar.edu.unlu.poo.burako.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa un juego (combinación de fichas) en la mesa de un jugador.
 *
 * MODIFICADO respecto al original:
 * - Toda la lógica de validación (esEscalera, esPierna, pureza) fue extraída
 *   a ValidadorJuego, que tiene esa única responsabilidad.
 * - Juego ahora solo gestiona: contener fichas, delegar validación,
 *   mantener el tipo y calcular su puntaje propio.
 * - JuegoMostrable ahora expone getFichas() (renombrado desde getJuego())
 *   y getTipo(), eliminando la ambigüedad entre el objeto y su contenido.
 * - Implementa JuegoMostrable usando List<FichaMostrable> para no exponer
 *   la lista interna de Ficha.
 */
public class Juego implements JuegoMostrable {

    private final List<Ficha> fichas;
    private TipoJuego tipo;

    /**
     * Crea un juego a partir de una lista de fichas.
     * @throws Exception si las fichas no forman una combinación válida.
     */
    public Juego(List<Ficha> fichas) throws Exception {
        this.fichas = new ArrayList<>(fichas);
        this.tipo   = ValidadorJuego.clasificar(this.fichas);
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

    // ── Operaciones internas (usadas por Jugador) ──────────────────────────────

    /**
     * Agrega una ficha en la posición {@code pos} (1-based) del juego.
     * Si la ficha real reemplaza a un comodín, el comodín se desplaza al final.
     *
     * @throws Exception si la posición o la ficha no son válidas para este juego.
     */
    void agregar(Ficha ficha, int pos) throws Exception {
        if (pos < 1 || pos > fichas.size() + 1) {
            throw new Exception("Posición " + pos + " inválida para el juego (tamaño: " + fichas.size() + ").");
        }

        int posIdx = pos - 1; // convertir a 0-based

        if (!ValidadorJuego.esAgregadoValido(fichas, tipo, ficha, posIdx)) {
            throw new Exception("La ficha " + ficha + " no es válida en la posición " + pos + " del juego.");
        }

        // Si se inserta una ficha real donde hay un comodín, el comodín se va al final
        boolean desplazaComodin = posIdx < fichas.size()
                && fichas.get(posIdx).esComodin()
                && !ficha.esComodin()
                && (tipo == TipoJuego.Escalera || tipo == TipoJuego.CanastaImpuraEscalera
                || tipo == TipoJuego.CanastaPuraEscalera);

        if (desplazaComodin) {
            Ficha comodinDesplazado = fichas.remove(posIdx);
            fichas.add(posIdx, ficha);
            fichas.add(comodinDesplazado); // al final
        } else {
            fichas.add(posIdx, ficha);
        }

        // Reclasificar si alcanzamos 7 fichas
        if (fichas.size() >= 7) {
            tipo = ValidadorJuego.reclasificarComoCanasta(fichas);
        }
    }

    /** Retorna la cantidad de fichas en este juego. */
    int cantFichas() {
        return fichas.size();
    }

    /**
     * Calcula el puntaje de este juego según las reglas de Burako.
     * Las canastas suman un bono adicional.
     */
    int calcularPuntaje() {
        int puntos = fichas.stream().mapToInt(Ficha::getValor).sum();
        if (tipo.esPura()) {
            puntos += 200;
        } else if (tipo.esCanasta()) {
            puntos += 100;
        }
        return puntos;
    }
}

