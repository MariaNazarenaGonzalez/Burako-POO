package ar.edu.unlu.poo.burako.persistencia;

import java.io.Serializable;

/**
 * Fila de la tabla de posiciones (ranking) correspondiente a un
 * {@link Usuario}.
 *
 * Es una proyección de solo lectura de {@link EstadisticasUsuario}, pensada
 * para que el módulo de Ranking pueda publicarse, ordenarse y evolucionar
 * (por ejemplo, en el futuro un ranking por temporada o por modalidad)
 * de forma independiente de la entidad Usuario.
 *
 * Deliberadamente NO referencia {@code modelo.Jugador} ni {@code modelo.Juego}:
 * el Ranking es un módulo de la capa de persistencia que solo conoce
 * Usuario, nunca participantes ni partidas en curso.
 */
public class EntradaRanking implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String idUsuario;
    private String nombre;
    private int partidasJugadas;
    private int victorias;
    private int derrotas;
    private int puntajeAcumulado;

    public EntradaRanking(String idUsuario, String nombre) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
    }

    /** Sincroniza esta entrada con el estado actual de las estadísticas del usuario. */
    public void actualizar(String nombre, EstadisticasUsuario estadisticas) {
        this.nombre = nombre;
        this.partidasJugadas = estadisticas.getPartidasJugadas();
        this.victorias = estadisticas.getVictorias();
        this.derrotas = estadisticas.getDerrotas();
        this.puntajeAcumulado = estadisticas.getPuntajeAcumulado();
    }

    public String getIdUsuario()     { return idUsuario; }
    public String getNombre()        { return nombre; }
    public int getPartidasJugadas()  { return partidasJugadas; }
    public int getVictorias()        { return victorias; }
    public int getDerrotas()         { return derrotas; }
    public int getPuntajeAcumulado() { return puntajeAcumulado; }

    @Override
    public String toString() {
        return nombre + " - PJ:" + partidasJugadas + " V:" + victorias
                + " D:" + derrotas + " Pts:" + puntajeAcumulado;
    }
}
