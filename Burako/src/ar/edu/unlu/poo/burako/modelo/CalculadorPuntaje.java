package ar.edu.unlu.poo.burako.modelo;

import java.util.List;

/**
 * Calcula el puntaje final de un jugador según las reglas del juego.
 *
 * El cálculo contempla los puntos obtenidos en los juegos realizados,
 * las fichas restantes en el atril y las bonificaciones o penalizaciones
 * correspondientes al cierre de la partida y a la toma del muerto.
 */
final class CalculadorPuntaje {

    private CalculadorPuntaje() {}

    /**
     * Calcula el puntaje del jugador según las reglas de Burako.
     *
     * Reglas:
     * - Si el jugador tiene al menos una canasta: puntosJuegos - puntosAtril.
     * - Si no tiene canasta: -(puntosJuegos + puntosAtril) (todo resta).
     * - Bono por cortar: +100.
     * - Bono por tomar el muerto: +100.
     * - Penalización por no tomar el muerto: -100.
     *
     * @param juegos       lista de juegos bajados por el jugador
     * @param atrilFichas  lista de fichas que le quedaron en el atril
     * @param tieneCanasta true si el jugador tiene al menos una canasta bajada
     * @param corto        true si este jugador fue quien cerró la partida
     * @param tomoMuerto   true si el jugador tomó su muerto durante la partida
     */
    static int calcular(List<Juego> juegos, List<Ficha> atrilFichas,
                        boolean tieneCanasta, boolean corto, boolean tomoMuerto) {

        int puntosJuegos = juegos.stream().mapToInt(Juego::calcularPuntaje).sum();
        int puntosAtril  = atrilFichas.stream().mapToInt(Ficha::getValor).sum();

        int total;
        if (tieneCanasta) {
            total = puntosJuegos - puntosAtril;
        } else {
            total = -(puntosJuegos + puntosAtril);
        }

        if (corto)      total += 100;
        if (tomoMuerto) total += 100;
        else            total -= 100;

        return total;
    }
}
