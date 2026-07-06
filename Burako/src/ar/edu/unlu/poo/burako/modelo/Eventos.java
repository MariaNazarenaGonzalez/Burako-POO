package ar.edu.unlu.poo.burako.modelo;

/**
 * Enumeración que contiene todos los eventos que el
 * modelo puede notificar a sus observadores durante
 * el desarrollo de una partida.
 *
 * Cada constante representa el resultado exitoso o
 * fallido de una acción realizada por un jugador.
 */
public enum Eventos {
    tomarMazo_NO_exitoso,
    tomarMazo_exitoso,
    tomarPozo_NO_exitoso,
    tomarPozo_exitoso,
    bajarJuego_NO_exitoso,
    bajarJuego_exitoso,
    agregarPozo_NO_exitoso,
    agregarPozo_exitoso,
    apoyarJuego_NO_exitoso,
    apoyarJuego_exitoso,
    tomarMuerto_exitoso,
    cortar_exitoso,
    cortar_NO_exitoso,
    partida_terminada
}
