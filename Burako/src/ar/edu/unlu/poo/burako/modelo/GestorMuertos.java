package ar.edu.unlu.poo.burako.modelo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Gestiona los muertos disponibles en la partida.
 *
 * NUEVA - Extraída de Burako para eliminar la lógica duplicada que aparecía
 * tres veces (en agregarPozo(), bajarJuego() y apoyarJuego()):
 *
 *   if (!jugador.yaTomoMuerto() && !muertos.isEmpty()) {
 *       ArrayList<Ficha> fichasMuerto = muertos.remove(0).tomar();
 *       jugador.agregarAtril(fichasMuerto);
 *       jugador.setYaTomoMuerto(true);
 *   }
 *
 * Ahora esa responsabilidad vive aquí, con una única firma clara.
 * Burako delega a GestorMuertos sin duplicar código.
 */
class GestorMuertos {

    private final Deque<Muerto> muertos;

    GestorMuertos(List<Ficha> fichasMuerto1, List<Ficha> fichasMuerto2) {
        muertos = new ArrayDeque<>();
        muertos.add(new Muerto(fichasMuerto1));
        muertos.add(new Muerto(fichasMuerto2));
    }

    /**
     * Intenta asignar el primer muerto disponible al jugador.
     * Si el jugador ya tomó su muerto o no quedan muertos, no hace nada.
     *
     * @param jugador el jugador que podría recibir el muerto
     * @return true si se le asignó un muerto al jugador en esta llamada
     */
    boolean intentarAsignarMuerto(Jugador jugador) {
        if (jugador.yaTomoMuerto() || muertos.isEmpty()) {
            return false;
        }
        Muerto siguiente = muertos.poll();
        jugador.agregarAtril(siguiente.tomar());
        jugador.marcarMuertoTomado();
        return true;
    }

    /** Retorna true si quedan muertos sin tomar. */
    boolean hayMuertosDisponibles() {
        return !muertos.isEmpty();
    }
}