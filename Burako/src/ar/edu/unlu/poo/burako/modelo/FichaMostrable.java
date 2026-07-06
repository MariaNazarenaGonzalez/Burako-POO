package ar.edu.unlu.poo.burako.modelo;

import java.io.Serializable;

/**
 * Define una vista de solo lectura de una ficha del juego Burako.
 *
 * Esta interfaz expone únicamente la información necesaria para
 * consultar las características de una ficha, evitando que las
 * capas externas al modelo puedan modificar su estado.
 *
 * Es utilizada por el Controlador y la Vista para acceder a los
 * datos de una ficha manteniendo el encapsulamiento del modelo.
 *
 * Al extender {@link Serializable}, sus implementaciones pueden
 * ser transmitidas entre distintos procesos o equipos cuando la
 * aplicación utiliza mecanismos de comunicación remota.
 */
public interface FichaMostrable extends Serializable {
    FichaColor getColor();
    FichaNumero getNum();
}
