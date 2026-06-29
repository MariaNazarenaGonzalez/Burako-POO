package ar.edu.unlu.poo.burako.modelo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Gestiona el stock de muertos disponibles en la partida.
 *
 * Responsabilidad única: administrar la cola de muertos y entregarlos cuando
 * Burako lo ordena. NO decide si corresponde entregar un muerto: esa decisión
 * la toman ReglasDeJuego.correspondeTomaMuertoDirecta/Indirecta().
 *
 * Burako pregunta a ReglasDeJuego si corresponde, y si corresponde,
 * llama asignarMuerto(jugador) aquí.
 */
class GestorMuertos {

    private final Deque<Muerto> muertos;

    GestorMuertos(List<Ficha> fichasMuerto1, List<Ficha> fichasMuerto2) {
        muertos = new ArrayDeque<>();
        muertos.add(new Muerto(fichasMuerto1));
        muertos.add(new Muerto(fichasMuerto2));
    }

    /**
     * Entrega el siguiente muerto disponible al jugador.
     * Precondición: hayMuertosDisponibles() == true.
     * La precondición es verificada por ReglasDeJuego antes de esta llamada.
     */
    void asignarMuerto(Jugador jugador) {
        Muerto siguiente = muertos.poll();
        jugador.agregarAtril(siguiente.tomar());
        jugador.marcarMuertoTomado();
    }

    /** Retorna true si hay al menos un muerto sin tomar. */
    boolean hayMuertosDisponibles() {
        return !muertos.isEmpty();
    }
}
