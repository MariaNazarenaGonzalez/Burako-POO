package ar.edu.unlu.poo.burako.modelo;

import java.util.List;

/**
 * Vista de solo lectura de un Juego (combinación de fichas en la mesa).
 * Expuesta al Controlador y la Vista sin revelar la implementación interna.
 */
public interface JuegoMostrable {
    /** Retorna las fichas que componen este juego, en orden. */
    List<FichaMostrable> getFichas();

    /** Retorna el tipo clasificatorio de este juego. */
    TipoJuego getTipo();
}