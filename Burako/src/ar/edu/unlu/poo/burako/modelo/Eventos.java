package ar.edu.unlu.poo.burako.modelo;

/**
 * Eventos que el modelo emite hacia los observadores.
 * Sin cambios respecto al original; mantener estos nombres preserva compatibilidad
 * con el Controlador y las Vistas existentes.
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
