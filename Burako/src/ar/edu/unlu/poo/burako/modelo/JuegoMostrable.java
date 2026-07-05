package ar.edu.unlu.poo.burako.modelo;

import java.io.Serializable;
import java.util.List;

/**
 * Vista de solo lectura de un Juego (combinación de fichas en la mesa).
 * Expuesta al Controlador y la Vista sin revelar la implementación interna.
 *
 * MODIFICADO (Fase 8 - Preparación RMIMVC): extiende Serializable, por la
 * misma razón que FichaMostrable: es un tipo que IBurako.getJuegos()
 * expone hacia afuera y debe poder viajar por RMI.
 */
public interface JuegoMostrable extends Serializable {
    /** Retorna las fichas que componen este juego, en orden. */
    public List<FichaMostrable> getFichas();

    /** Retorna el tipo clasificatorio de este juego. */
    public TipoJuego getTipo();
}
