package ar.edu.unlu.poo.burako.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa el estado de un jugador durante la partida.
 *
 * Responsabilidad única: mantener el estado del jugador (atril, juegos bajados,
 * muerto tomado) y ejecutar operaciones estructurales sobre ese estado.
 *
 * NO contiene validaciones de reglas. Todas las validaciones son realizadas
 * por ReglasDeJuego antes de que Burako invoque los métodos de esta clase.
 * Si se invoca un método con datos inválidos, se lanza Exception como
 * protección ante uso incorrecto del API interno.
 *
 * Cambios respecto a la versión anterior:
 * - Eliminada referencia circular a Burako.
 * - calcularPuntaje() delega completamente a ReglasDeJuego.calcularPuntaje().
 * - bajarJuego() y apoyarJuego() no validan reglas, solo ejecutan.
 * - Acceso de paquete para métodos invocados por Burako y GestorMuertos.
 *
 * MODIFICADO (Fase 6 - Persistencia): implementa Serializable para formar
 * parte del estado guardable de una partida (es referenciado desde Burako).
 */
public class Jugador implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Atril       atril;
    private final List<Juego> juegos;
    private boolean           yaTomoMuerto;
    private String            nombre;

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

    /** Compatibilidad con tests existentes. */
    public void setYaTomoMuerto(boolean v) { this.yaTomoMuerto = v; }

    // ── Atril ─────────────────────────────────────────────────────────────────

    void agregarAtril(List<Ficha> nuevas) { atril.agregar(nuevas); }
    void agregarAtril(Ficha ficha)        { atril.agregar(ficha); }

    /**
     * Retorna la ficha en la posición 1-based sin removerla.
     * @throws Exception si la posición no existe.
     */
    Ficha verAtril(int pos) throws Exception {
        return atril.ver(new int[]{pos}).get(0);
    }

    /**
     * Remueve la ficha dada del atril.
     * @throws Exception si la ficha no está en el atril.
     */
    void sacarAtril(Ficha ficha) throws Exception {
        atril.sacar(List.of(ficha));
    }

    /** Vista de solo lectura del atril (para IBurako → Controlador). */
    public List<FichaMostrable> getAtril() {
        return atril.get();
    }

    /** Acceso tipado sin cast, para ReglasDeJuego.calcularPuntaje(). */
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
     * Baja un nuevo juego con las fichas en las posiciones 1-based indicadas.
     * Precondición: ReglasDeJuego.validarBajarJuego() aprobó la operación.
     * @throws Exception si hay error estructural (posición inválida, combinación inválida).
     */
    void bajarJuego(int[] posicionesAtril) throws Exception {
        List<Ficha> seleccionadas = atril.ver(posicionesAtril);
        Juego nuevo = new Juego(seleccionadas);
        atril.sacar(seleccionadas);
        juegos.add(nuevo);
    }

    /**
     * Apoya la ficha en posAtril (1-based) sobre el juego numJuego (1-based)
     * en la posición posEnJuego (1-based) de ese juego.
     * Precondición: ReglasDeJuego.validarApoyarJuego() aprobó la operación.
     * @throws Exception si algún índice es inválido estructuralmente.
     */
    void apoyarJuego(int posAtril, int posEnJuego, int numJuego) throws Exception {
        if (numJuego < 1 || numJuego > juegos.size()) {
            throw new Exception("El juego " + numJuego + " no existe (total: " + juegos.size() + ").");
        }
        Ficha ficha = verAtril(posAtril);
        juegos.get(numJuego - 1).agregar(ficha, posEnJuego);
        sacarAtril(ficha);
    }

    /** Vista de solo lectura de los juegos bajados (para IBurako → Controlador). */
    public List<JuegoMostrable> getJugadas() {
        return Collections.unmodifiableList(juegos);
    }

    public int cantJuegos() { return juegos.size(); }

    public boolean tieneCanasta() {
        return juegos.stream().anyMatch(j -> j.getTipo().esCanasta());
    }

    // ── Puntaje ───────────────────────────────────────────────────────────────

    /**
     * Calcula el puntaje final delegando a ReglasDeJuego, que es la única
     * fuente de verdad para las reglas de puntaje.
     *
     * @param corto true si este jugador fue quien cerró la partida.
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
     * Retorna las fichas internas del juego en la posición numJuego (1-based).
     * Usado por Burako para construir el ContextoJugada al apoyar.
     */
    List<Ficha> getFichasDeJuego(int numJuego) {
        if (numJuego < 1 || numJuego > juegos.size()) return List.of();
        return juegos.get(numJuego - 1).getFichasInternas();
    }

    /**
     * Retorna el TipoJuego del juego en la posición numJuego (1-based).
     * Usado por Burako para construir el ContextoJugada al apoyar.
     */
    TipoJuego getTipoDeJuego(int numJuego) {
        if (numJuego < 1 || numJuego > juegos.size()) return null;
        return juegos.get(numJuego - 1).getTipo();
    }
}
