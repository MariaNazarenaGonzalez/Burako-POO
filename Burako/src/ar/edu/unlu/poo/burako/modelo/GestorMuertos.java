package ar.edu.unlu.poo.burako.modelo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Administra los muertos disponibles durante una partida de Burako.
 *
 * Esta clase es responsable de almacenar los muertos de cada equipo
 * y entregarlos cuando el reglamento del juego lo permite.
 *
 * No determina cuándo corresponde asignar un muerto; esa validación
 * es realizada por la lógica de reglas del juego. Su única función
 * consiste en gestionar la disponibilidad y asignación de los muertos
 * a los jugadores del equipo correspondiente.
 *
 * Los muertos se almacenan asociados a cada equipo para garantizar
 * que cada uno reciba únicamente el que le corresponde.
 */
class GestorMuertos implements Serializable {

    private static final long serialVersionUID = 2L;

    private final Map<Integer, Muerto> muertosPorEquipo;
    /**
     * Crea el gestor de muertos de la partida.
     *
     * @param fichasMuerto1 fichas que conforman el muerto del primer equipo.
     * @param fichasMuerto2 fichas que conforman el muerto del segundo equipo.
     */
    GestorMuertos(List<Ficha> fichasMuerto1, List<Ficha> fichasMuerto2) {
        muertosPorEquipo = new HashMap<>();
        muertosPorEquipo.put(0, new Muerto(fichasMuerto1));
        muertosPorEquipo.put(1, new Muerto(fichasMuerto2));
    }

    /**
     * Asigna al jugador el muerto correspondiente a su equipo.
     *
     * Se asume que previamente se verificó la disponibilidad del muerto
     * para el equipo indicado.
     *
     * @param jugador jugador que recibirá el muerto.
     * @param equipo identificador del equipo al que pertenece el jugador.
     */
    void asignarMuerto(Jugador jugador, int equipo) {
        Muerto muerto = muertosPorEquipo.remove(equipo);
        jugador.agregarAtril(muerto.tomar());
        jugador.marcarMuertoTomado();
    }

    /**
     * Indica si el equipo especificado aún dispone de su muerto.
     *
     * @param equipo identificador del equipo.
     * @return {@code true} si el muerto continúa disponible;
     *         {@code false} en caso contrario.
     */
    boolean hayMuertoDisponibleParaEquipo(int equipo) {
        return muertosPorEquipo.containsKey(equipo);
    }

    /**
     * Verifica si todavía existe al menos un muerto disponible
     * para ser asignado.
     *
     * @return {@code true} si queda algún muerto sin entregar;
     *         {@code false} en caso contrario.
     */
    boolean hayMuertosDisponibles() {
        return !muertosPorEquipo.isEmpty();
    }
}
