package ar.edu.unlu.poo.burako.modelo;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase auxiliar EXCLUSIVA para tests. No forma parte del modelo de producción.
 *
 * Objetivo: eliminar el boilerplate repetido en los tests anteriores (vaciar
 * atril a mano ficha por ficha, construir combinaciones válidas ficha por
 * ficha, calcular índices 1-based para bajarJuego, etc.) detrás de métodos
 * de fábrica con nombres declarativos.
 *
 * Vive en el mismo paquete que el modelo (ar.edu.unlu.poo.burako.modelo)
 * porque necesita acceso de paquete a miembros que Burako/Jugador no
 * exponen públicamente (por ejemplo, el constructor de Jugador), tal como
 * ya hacían los tests anteriores. No se modificó ningún método de
 * visibilidad en el modelo de producción para lograr esto.
 *
 * Nota de nomenclatura: esta versión del proyecto no tiene una clase
 * "Mesa" independiente (el estado de la mesa —pozo, juegos bajados,
 * muertos— vive dentro de Burako). Por eso no existe crearMesa(); en su
 * lugar, crearBurako()/crearPartidaNueva() cubren ese rol.
 */
public final class TestDataFactory {

    private TestDataFactory() {
        // Clase utilitaria, no instanciable.
    }

    // ── Fichas y combinaciones ──────────────────────────────────────────────

    /** Crea una ficha individual. Alias legible sobre el constructor de Ficha. */
    public static Ficha crearFicha(FichaColor color, FichaNumero numero) {
        return new Ficha(color, numero);
    }

    /**
     * Crea una escalera de {@code cantidad} fichas del mismo color, empezando
     * en {@code desde} y avanzando con FichaNumero.siguiente() (circular:
     * ...N13 -> N1, tal como permiten las reglas de Burako).
     */
    public static List<Ficha> crearEscalera(FichaColor color, FichaNumero desde, int cantidad) {
        List<Ficha> fichas = new ArrayList<>();
        FichaNumero actual = desde;
        for (int i = 0; i < cantidad; i++) {
            fichas.add(new Ficha(color, actual));
            actual = actual.siguiente();
        }
        return fichas;
    }

    /**
     * Crea una pierna de {@code cantidad} fichas del mismo número, en colores
     * distintos (cicla FichaColor.values() si cantidad > 4).
     */
    public static List<Ficha> crearPierna(FichaNumero numero, int cantidad) {
        List<Ficha> fichas = new ArrayList<>();
        FichaColor[] colores = FichaColor.values();
        for (int i = 0; i < cantidad; i++) {
            fichas.add(new Ficha(colores[i % colores.length], numero));
        }
        return fichas;
    }

    /** Canasta limpia (7 fichas, escalera de un solo color, sin comodines). */
    public static List<Ficha> crearCanastaLimpia(FichaColor color, FichaNumero desde) {
        return crearEscalera(color, desde, 7);
    }

    /** Canasta sucia: 6 fichas en escalera + 1 comodín negro al final. */
    public static List<Ficha> crearCanastaSucia(FichaColor color, FichaNumero desde) {
        List<Ficha> fichas = crearEscalera(color, desde, 6);
        fichas.add(new Ficha(FichaColor.Negro, FichaNumero.Comodin));
        return fichas;
    }

    // ── Jugador ──────────────────────────────────────────────────────────────

    /** Crea un Jugador con la mano inicial dada. */
    public static Jugador crearJugador(List<Ficha> manoInicial) {
        return new Jugador(manoInicial);
    }

    /** Crea un Jugador con el atril completamente vacío. */
    public static Jugador crearJugadorSinCartas() {
        return crearJugador(new ArrayList<>());
    }

    /** Crea un Jugador sin cartas que ya tomó su muerto (yaTomoMuerto() == true). */
    public static Jugador crearJugadorConMuerto() {
        Jugador jugador = crearJugadorSinCartas();
        jugador.setYaTomoMuerto(true);
        return jugador;
    }

    /**
     * Crea un Jugador con una canasta limpia ya bajada a la mesa y el atril
     * vacío. Útil para tests de puntaje y de condiciones de corte que no
     * necesitan una partida (Burako) completa.
     */
    public static Jugador crearJugadorConCanasta() throws Exception {
        Jugador jugador = crearJugadorSinCartas();
        List<Ficha> canasta = crearCanastaLimpia(FichaColor.Rojo, FichaNumero.N3);
        jugador.agregarAtril(canasta);
        jugador.bajarJuego(new int[]{1, 2, 3, 4, 5, 6, 7});
        return jugador;
    }

    // ── Burako / partidas ────────────────────────────────────────────────────

    /** Crea una partida nueva sin nombres asignados (equivalente a `new Burako()`). */
    public static Burako crearBurako() {
        return new Burako();
    }

    /** Crea una partida nueva lista para jugar, con nombres asignados. */
    public static Burako crearPartidaNueva() throws RemoteException {
        Burako burako = new Burako();
        burako.setNombres("Jugador 1", "Jugador 2");
        return burako;
    }

    /** Vacía por completo el atril del jugador {@code indice} dentro de una partida en curso. */
    public static void vaciarAtril(Burako burako, int indice) throws Exception {
        Jugador jugador = burako.getJugador(indice);
        List<FichaMostrable> actual = new ArrayList<>(jugador.getAtril());
        for (FichaMostrable f : actual) {
            jugador.sacarAtril((Ficha) f);
        }
    }

    /**
     * Hace que el jugador {@code indice} tome del mazo, transicionando su
     * estado de TOMAR a JUGAR. Retorna el resultado de la operación.
     */
    public static boolean tomarDelMazo(Burako burako, int indice) throws RemoteException {
        return burako.agarrarMazo(indice);
    }

    /**
     * Deja al jugador {@code indice} en la posición exacta previa a un corte
     * exitoso: atril vacío salvo 1 ficha, una canasta ya bajada, y el muerto
     * ya tomado. El llamador solo necesita descartar esa última ficha
     * (posición 1) para cerrar la partida.
     *
     * Requiere que sea el turno de {@code indice} y que el estado sea TOMAR
     * (condición por defecto al recibir una partida recién creada).
     */
    public static void prepararParaCorte(Burako burako, int indice, FichaColor color, FichaNumero desde)
            throws Exception {
        tomarDelMazo(burako, indice); // TOMAR -> JUGAR

        Jugador jugador = burako.getJugador(indice);
        vaciarAtril(burako, indice);

        List<Ficha> canasta = crearCanastaLimpia(color, desde);
        jugador.agregarAtril(canasta);
        jugador.bajarJuego(new int[]{1, 2, 3, 4, 5, 6, 7});

        jugador.setYaTomoMuerto(true);
        jugador.agregarAtril(new Ficha(FichaColor.Azul, FichaNumero.N9)); // última ficha a descartar
    }

    /**
     * Crea una partida ya finalizada (alguien cortó): el jugador 0 tiene una
     * canasta bajada, tomó su muerto, y acaba de descartar su última ficha,
     * cerrando la partida (EstadoTurno.PARTIDA_TERMINADA).
     */
    public static Burako crearPartidaFinalizada() throws Exception {
        Burako burako = crearPartidaNueva();
        prepararParaCorte(burako, 0, FichaColor.Rojo, FichaNumero.N3);
        burako.agregarPozo(1, 0); // descarta la última ficha -> corte
        return burako;
    }
}
