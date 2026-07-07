package ar.edu.unlu.poo.burako.persistencia;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Contrato remoto que expone el ranking de jugadores.
 * Se publica en el mismo registro RMI que el modelo de la partida,
 * bajo un nombre distinto, para que cualquier cliente pueda consultarlo
 * sin unirse a la partida ni exponer un puerto propio de escucha.
 */
public interface IServicioRanking extends Remote {

    /** Nombre con el que se publica este servicio en el registro RMI del servidor. */
    String NOMBRE_REGISTRO = "MVCRMI/Ranking";

    /** Retorna el ranking completo, ordenado de mayor a menor puntaje. */
    List<EntradaRanking> obtenerRanking() throws RemoteException;
} 