package ar.edu.unlu.poo.burako.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa a un jugador participante de una partida de Burako.
 *
 * Cada jugador mantiene su nombre, las fichas disponibles en su
 * atril, los juegos que ha bajado a la mesa y el estado de toma
 * del muerto.
 *
 * Además, proporciona las operaciones necesarias para administrar
 * estos elementos durante el desarrollo de la partida, mientras
 * que la validación de las reglas del juego es responsabilidad de
 * otras clases del modelo.
 */
public class Jugador implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Atril       atril;
    private final List<Juego> juegos;
    private boolean           yaTomoMuerto;
    private String            nombre;
    /**
     * Crea un jugador con las fichas iniciales de su atril.
     *
     * @param fichasIniciales fichas entregadas al comenzar la partida.
     */
    Jugador(List<Ficha> fichasIniciales) {
        this.atril        = new Atril(fichasIniciales);
        this.juegos       = new ArrayList<>();
        this.yaTomoMuerto = false;
        this.nombre       = "";
    }

    // ── Nombre ────────────────────────────────────────────────────────────────

    public String getNombre()           { return nombre; }
    public void   setNombre(String n)   { this.nombre = n; }

    // ── Estado del muerto ─────────────────────────────────────────────────────

    public boolean yaTomoMuerto()       { return yaTomoMuerto; }

    /** Invocado por GestorMuertos al entregar el muerto. */
    void marcarMuertoTomado()           { this.yaTomoMuerto = true; }

    /**
     * Modifica manualmente el estado de toma del muerto.
     *
     * Este método se utiliza principalmente durante pruebas.
     *
     * @param v nuevo estado del jugador respecto al muerto.
     */
    public void setYaTomoMuerto(boolean v) { this.yaTomoMuerto = v; }

    // ── Atril ─────────────────────────────────────────────────────────────────

    void agregarAtril(List<Ficha> nuevas) { atril.agregar(nuevas); }
    void agregarAtril(Ficha ficha)        { atril.agregar(ficha); }

    /**
     * Obtiene la ficha ubicada en una posición del atril sin retirarla.
     *
     * @param pos posición de la ficha (comenzando en 1).
     * @return ficha ubicada en la posición indicada.
     * @throws Exception si la posición es inválida.
     */
    Ficha verAtril(int pos) throws Exception {
        return atril.ver(new int[]{pos}).get(0);
    }

    /**
     * Elimina una ficha del atril.
     *
     * @param ficha ficha que se desea retirar.
     * @throws Exception si la ficha no pertenece al atril.
     */
    void sacarAtril(Ficha ficha) throws Exception {
        atril.sacar(List.of(ficha));
    }

    /**
     * Obtiene una vista de solo lectura del atril.
     *
     * @return fichas disponibles en el atril del jugador.
     */
    public List<FichaMostrable> getAtril() {
        return atril.get();
    }

    /**
     * Obtiene las fichas del atril para uso interno del modelo.
     *
     * @return copia de las fichas del atril.
     */
    List<Ficha> getAtrilInterno() {
        List<Ficha> copia = new ArrayList<>();
        for (FichaMostrable fm : atril.get()) {
            copia.add((Ficha) fm); // seguro: Atril solo almacena Ficha
        }
        return copia;
    }

    boolean atrilVacio() { return atril.estaVacio(); }

    // ── Juegos ────────────────────────────────────────────────────────────────

    /**
     * Forma un nuevo juego utilizando las fichas indicadas del atril.
     *
     * @param posicionesAtril posiciones de las fichas seleccionadas.
     * @throws Exception si ocurre un error al crear el juego.
     */
    void bajarJuego(int[] posicionesAtril) throws Exception {
        List<Ficha> seleccionadas = atril.ver(posicionesAtril);
        Juego nuevo = new Juego(seleccionadas);
        atril.sacar(seleccionadas);
        juegos.add(nuevo);
    }

    /**
     * Agrega una ficha del atril a un juego existente.
     *
     * @param posAtril posición de la ficha en el atril.
     * @param posEnJuego posición donde se insertará la ficha.
     * @param numJuego juego sobre el que se realizará el apoyo.
     * @throws Exception si alguna posición es inválida.
     */
    void apoyarJuego(int posAtril, int posEnJuego, int numJuego) throws Exception {
        if (numJuego < 1 || numJuego > juegos.size()) {
            throw new Exception("El juego " + numJuego + " no existe (total: " + juegos.size() + ").");
        }
        Ficha ficha = verAtril(posAtril);
        juegos.get(numJuego - 1).agregar(ficha, posEnJuego);
        sacarAtril(ficha);
    }

    /**
     * Obtiene los juegos que el jugador ha bajado a la mesa.
     *
     * @return lista de juegos del jugador.
     */
    public List<JuegoMostrable> getJugadas() {
        return Collections.unmodifiableList(juegos);
    }
    /**
     * Obtiene la cantidad de juegos bajados.
     *
     * @return cantidad de juegos.
     */
    public int cantJuegos() { return juegos.size(); }
    /**
     * Indica si el jugador posee al menos una canasta.
     *
     * @return {@code true} si existe una canasta; {@code false} en caso contrario.
     */
    public boolean tieneCanasta() {
        return juegos.stream().anyMatch(j -> j.getTipo().esCanasta());
    }

    // ── Puntaje ───────────────────────────────────────────────────────────────

    /**
     * Calcula el puntaje final obtenido por el jugador.
     *
     * @param corto indica si el jugador fue quien cerró la partida.
     * @return puntaje total del jugador.
     */
    public int calcularPuntaje(boolean corto) {
        return ReglasDeJuego.calcularPuntaje(
                juegos,
                getAtrilInterno(),
                tieneCanasta(),
                corto,
                yaTomoMuerto
        );
    }

    // ── Acceso interno a juegos (para ReglasDeJuego.validarApoyarJuego) ───────

    /**
     * Obtiene las fichas que componen un juego determinado.
     *
     * @param numJuego número del juego (comenzando en 1).
     * @return fichas del juego indicado o una lista vacía si no existe.
     */
    List<Ficha> getFichasDeJuego(int numJuego) {
        if (numJuego < 1 || numJuego > juegos.size()) return List.of();
        return juegos.get(numJuego - 1).getFichasInternas();
    }

    /**
     * Obtiene el tipo correspondiente a un juego del jugador.
     *
     * @param numJuego número del juego (comenzando en 1).
     * @return tipo del juego o {@code null} si no existe.
     */
    TipoJuego getTipoDeJuego(int numJuego) {
        if (numJuego < 1 || numJuego > juegos.size()) return null;
        return juegos.get(numJuego - 1).getTipo();
    }
}
