package ar.edu.unlu.poo.burako.modelo;

import java.io.Serializable;

/**
 * Vista de solo lectura de una Ficha, expuesta hacia el Controlador y la Vista.
 * El Controlador y la Vista nunca reciben objetos Ficha directamente,
 * solo FichaMostrable, preservando el encapsulamiento del modelo.
 *
 * MODIFICADO (Fase 8 - Preparación RMIMVC): extiende Serializable.
 * Es el tipo que IBurako.getPozo()/getAtril() exponen realmente hacia
 * afuera; exigir Serializable a nivel de interfaz garantiza que
 * cualquier implementación futura de FichaMostrable sea transportable
 * por RMI, en lugar de depender de que Ficha "casualmente" lo sea.
 */
public interface FichaMostrable extends Serializable {
    FichaColor getColor();
    FichaNumero getNum();
}
