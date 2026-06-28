package ar.edu.unlu.poo.burako.modelo;

public interface Observado {
    void agregarObservador(Observador obsevador);
    void notificarObservador(Eventos eventos);
}
