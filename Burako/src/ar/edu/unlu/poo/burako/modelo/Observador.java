package ar.edu.unlu.poo.burako.modelo;

public interface Observador {
    void notificar(Eventos eventos);

    void notificar(Eventos eventos, Exception e);
}
