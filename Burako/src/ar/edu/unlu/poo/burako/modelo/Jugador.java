package ar.edu.unlu.poo.burako.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa a un jugador en una partida de Burako.
 *
 * MODIFICADO respecto al original:
 * - Eliminada la referencia circular {@code private final Burako burako}.
 *   Era un campo que nunca se usaba y que habría causado StackOverflowError
 *   al serializar el grafo de objetos.
 * - calcularPuntaje() delegado a CalculadorPuntaje (SRP: Jugador no calcula puntos).
 * - El cast inseguro {@code (Ficha) f} del original fue eliminado: CalculadorPuntaje
 *   trabaja directamente con la lista interna de Ficha.
 * - marcarMuertoTomado() reemplaza a setYaTomoMuerto(true): expresa intención.
 * - Los métodos de acceso interno (bajarJuego, apoyarJuego, sacarAtril,
 *   agregarAtril) son package-private para que solo Burako y GestorMuertos
 *   los invoquen; la Vista nunca llega a Jugador directamente.
 * - getAtrilInterno() es package-private para que CalculadorPuntaje pueda
 *   trabajar con Ficha sin necesidad de cast.
 */
public class Jugador {

    private final Atril atril;
    private final List<Juego> juegos;
    private boolean yaTomoMuerto;
    private String nombre;

    Jugador(List<Ficha> fichasIniciales) {
        this.atril        = new Atril(fichasIniciales);
        this.juegos       = new ArrayList<>();
        this.yaTomoMuerto = false;
        this.nombre       = "";
    }

    // ── Nombre ────────────────────────────────────────────────────────────────

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // ── Estado del muerto ─────────────────────────────────────────────────────

    public boolean yaTomoMuerto() {
        return yaTomoMuerto;
    }

    /** Usado exclusivamente por GestorMuertos al asignar el muerto. */
    void marcarMuertoTomado() {
        this.yaTomoMuerto = true;
    }

    /**
     * Acceso de compatibilidad para los tests existentes.
     * @deprecated Preferir marcarMuertoTomado() para toma real;
     *             setYaTomoMuerto(true/false) solo para setup de tests.
     */
    public void setYaTomoMuerto(boolean valor) {
        this.yaTomoMuerto = valor;
    }

    // ── Operaciones del atril ─────────────────────────────────────────────────

    /** Agrega fichas al atril (usada por GestorMuertos y Burako). */
    void agregarAtril(List<Ficha> nuevas) {
        atril.agregar(nuevas);
    }

    /** Agrega una sola ficha al atril (usada por Burako al tomar del mazo). */
    void agregarAtril(Ficha ficha) {
        atril.agregar(ficha);
    }

    /**
     * Retorna la ficha en la posición {@code posicion} (1-based) sin removerla.
     * @throws Exception si la posición no existe.
     */
    Ficha verAtril(int posicion) throws Exception {
        return atril.ver(new int[]{posicion}).get(0);
    }

    /**
     * Remueve una ficha del atril.
     * @throws Exception si la ficha no existe en el atril.
     */
    void sacarAtril(Ficha ficha) throws Exception {
        atril.sacar(List.of(ficha));
    }

    /** Vista pública del atril (para el Controlador). */
    public List<FichaMostrable> getAtril() {
        return atril.get();
    }

    /** Acceso interno a las fichas reales del atril (para CalculadorPuntaje). */
    List<Ficha> getAtrilInterno() {
        // Construcción segura: copia defensiva tipada
        List<Ficha> copia = new ArrayList<>();
        for (FichaMostrable fm : atril.get()) {
            copia.add((Ficha) fm); // seguro: Atril solo almacena Ficha
        }
        return copia;
    }

    /** Retorna true si el atril está vacío. */
    boolean atrilVacio() {
        return atril.estaVacio();
    }

    // ── Operaciones de juego ──────────────────────────────────────────────────

    /**
     * Baja un nuevo juego usando las fichas en las posiciones indicadas (1-based) del atril.
     * @throws Exception si hay menos de 3 fichas, posiciones inválidas o combinación no válida.
     */
    void bajarJuego(int[] posicionesAtril) throws Exception {
        if (posicionesAtril.length < 3) {
            throw new Exception("Un juego necesita al menos 3 fichas.");
        }
        List<Ficha> seleccionadas = atril.ver(posicionesAtril);
        Juego nuevoJuego = new Juego(seleccionadas); // lanza Exception si inválido
        atril.sacar(seleccionadas);
        juegos.add(nuevoJuego);
    }

    /**
     * Apoya la ficha en la posición {@code posAtril} (1-based) del atril
     * sobre el juego número {@code numJuego} (1-based) en la posición
     * {@code posJuego} (1-based) de ese juego.
     * @throws Exception si algún índice es inválido o la ficha no encaja.
     */
    void apoyarJuego(int posAtril, int posJuego, int numJuego) throws Exception {
        if (numJuego < 1 || numJuego > juegos.size()) {
            throw new Exception("El juego número " + numJuego + " no existe.");
        }
        Ficha ficha = verAtril(posAtril);
        juegos.get(numJuego - 1).agregar(ficha, posJuego);
        sacarAtril(ficha);
    }

    // ── Consultas de juegos ───────────────────────────────────────────────────

    /** Vista pública de los juegos bajados (para el Controlador). */
    public List<JuegoMostrable> getJugadas() {
        return Collections.unmodifiableList(juegos);
    }

    /** Retorna la cantidad de juegos bajados. */
    public int cantJuegos() {
        return juegos.size();
    }

    /**
     * Retorna true si el jugador tiene al menos una canasta (juego de 7+ fichas).
     */
    public boolean tieneCanasta() {
        return juegos.stream().anyMatch(j -> j.getTipo().esCanasta());
    }

    // ── Puntaje ───────────────────────────────────────────────────────────────

    /**
     * Calcula el puntaje final del jugador.
     * Delegado a CalculadorPuntaje para mantener SRP.
     *
     * @param corto true si este jugador fue quien cortó la partida.
     */
    public int calcularPuntaje(boolean corto) {
        return CalculadorPuntaje.calcular(
                juegos,
                getAtrilInterno(),
                tieneCanasta(),
                corto,
                yaTomoMuerto
        );
    }
}
