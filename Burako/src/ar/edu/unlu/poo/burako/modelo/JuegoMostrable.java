package ar.edu.unlu.poo.burako.modelo;

import java.io.Serializable;
import java.util.List;

/**
 * Define una vista de solo lectura de un juego formado sobre la mesa.
 *
 * Esta interfaz permite consultar las fichas que componen un juego
 * y su clasificación, sin exponer la implementación interna del
 * modelo.
 *
 * Al extender {@link Serializable}, sus implementaciones pueden ser
 * utilizadas tanto en entornos locales como remotos.
 */
public interface JuegoMostrable extends Serializable {
    /**
     * Obtiene las fichas que forman el juego.
     *
     * @return lista ordenada de fichas del juego.
     */
    List<FichaMostrable> getFichas();

    /**
     * Obtiene la clasificación del juego.
     *
     * @return tipo del juego.
     */
    TipoJuego getTipo();
}
