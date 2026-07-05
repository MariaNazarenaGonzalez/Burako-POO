package ar.edu.unlu.poo.burako.modelo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestiona el stock de muertos disponibles en la partida.
 *
 * Responsabilidad única: administrar los muertos y entregarlos cuando
 * Burako lo ordena. NO decide si corresponde entregar un muerto: esa decisión
 * la toman ReglasDeJuego.correspondeTomaMuertoDirecta/Indirecta().
 *
 * Burako pregunta a ReglasDeJuego si corresponde, y si corresponde,
 * llama asignarMuerto(jugador, equipo) aquí.
 *
 * MODIFICADO (Fase 6 - Persistencia): implementa Serializable; los muertos
 * pendientes forman parte del estado guardable de una partida.
 *
 * MODIFICADO (Fase 10 - Soporte 2 o 4 jugadores):
 * - Antes, los 2 muertos vivían en una cola FIFO genérica (ArrayDeque):
 *   cualquier jugador que corresponda tomaba "el siguiente" muerto,
 *   indistintamente. Esto era correcto con exactamente 2 jugadores (cada
 *   uno termina tomando como máximo un muerto, porque su propio
 *   yaTomoMuerto lo bloquea después), pero con 4 jugadores en 2 equipos
 *   sería incorrecto: un jugador del equipo A podría terminar tomando por
 *   error el muerto reservado para el equipo B si la cola no distingue
 *   equipos.
 * - Ahora los muertos se indexan explícitamente por equipo (Map<equipo,
 *   Muerto>): asignarMuerto(jugador, equipo) entrega SIEMPRE el muerto de
 *   ESE equipo, y lo remueve del mapa para que nadie más de ese equipo
 *   pueda tomarlo de nuevo. Con 2 jugadores, equipo == índice de jugador
 *   (ver Burako.equipoDe), por lo que el comportamiento observable es
 *   idéntico al de fases anteriores.
 */
class GestorMuertos implements Serializable {

    private static final long serialVersionUID = 2L;

    private final Map<Integer, Muerto> muertosPorEquipo;

    GestorMuertos(List<Ficha> fichasMuerto1, List<Ficha> fichasMuerto2) {
        muertosPorEquipo = new HashMap<>();
        muertosPorEquipo.put(0, new Muerto(fichasMuerto1));
        muertosPorEquipo.put(1, new Muerto(fichasMuerto2));
    }

    /**
     * Entrega al jugador el muerto correspondiente a su equipo.
     * Precondición: hayMuertoDisponibleParaEquipo(equipo) == true.
     * La precondición es verificada por ReglasDeJuego antes de esta llamada.
     */
    void asignarMuerto(Jugador jugador, int equipo) {
        Muerto muerto = muertosPorEquipo.remove(equipo);
        jugador.agregarAtril(muerto.tomar());
        jugador.marcarMuertoTomado();
    }

    /** Retorna true si el equipo dado todavía no tomó su muerto. */
    boolean hayMuertoDisponibleParaEquipo(int equipo) {
        return muertosPorEquipo.containsKey(equipo);
    }

    /** Retorna true si hay al menos un muerto sin tomar, en cualquier equipo. */
    boolean hayMuertosDisponibles() {
        return !muertosPorEquipo.isEmpty();
    }
}
