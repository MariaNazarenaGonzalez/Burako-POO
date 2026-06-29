package ar.edu.unlu.poo.burako.modelo;

/**
 * Vista de solo lectura de una Ficha, expuesta hacia el Controlador y la Vista.
 * El Controlador y la Vista nunca reciben objetos Ficha directamente,
 * solo FichaMostrable, preservando el encapsulamiento del modelo.
 */
public interface FichaMostrable {
    FichaColor getColor();
    FichaNumero getNum();
}