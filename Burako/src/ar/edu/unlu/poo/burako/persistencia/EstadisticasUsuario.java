package ar.edu.unlu.poo.burako.persistencia;

import java.io.Serializable;

/**
 * Estadísticas acumuladas de un {@link Usuario} a lo largo de todas las
 * partidas que jugó: cantidad de partidas, victorias, derrotas y puntaje
 * acumulado.
 *
 * Responsabilidad única: mantener estos cuatro contadores y exponer la
 * única operación que los mantiene consistentes entre sí
 * (registrarResultado), evitando que cada llamador incremente campos
 * sueltos y los desincronice.
 */
public class EstadisticasUsuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private int partidasJugadas;
    private int victorias;
    private int derrotas;
    private int puntajeAcumulado;

    public EstadisticasUsuario() {
        this(0, 0, 0, 0);
    }

    public EstadisticasUsuario(int partidasJugadas, int victorias, int derrotas, int puntajeAcumulado) {
        this.partidasJugadas = partidasJugadas;
        this.victorias = victorias;
        this.derrotas = derrotas;
        this.puntajeAcumulado = puntajeAcumulado;
    }

    /**
     * Registra el resultado de una partida recién finalizada.
     * @param gano true si el usuario ganó esa partida.
     * @param puntajeObtenido puntaje final obtenido en esa partida.
     */
    public void registrarResultado(boolean gano, int puntajeObtenido) {
        partidasJugadas++;
        if (gano) {
            victorias++;
        } else {
            derrotas++;
        }
        puntajeAcumulado += puntajeObtenido;
    }

    public int getPartidasJugadas() { return partidasJugadas; }
    public int getVictorias()       { return victorias; }
    public int getDerrotas()        { return derrotas; }
    public int getPuntajeAcumulado(){ return puntajeAcumulado; }

    @Override
    public String toString() {
        return "PJ=" + partidasJugadas + " V=" + victorias + " D=" + derrotas + " Pts=" + puntajeAcumulado;
    }
}
