package ar.edu.unlu.poo.burako.persistencia;

import java.util.List;

/**
 * Contrato de persistencia para el módulo de Ranking.
 * Módulo independiente: no referencia Juego ni Jugador, solo Usuario.
 */
public interface IRepositorioRanking {

    /** Crea o actualiza la entrada de ranking correspondiente al usuario dado. */
    void actualizar(Usuario usuario);

    /** Retorna el ranking completo, ordenado de mayor a menor puntaje acumulado. */
    List<EntradaRanking> obtenerRankingOrdenado();
}
