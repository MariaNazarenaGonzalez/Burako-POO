package ar.edu.unlu.poo.burako.persistencia;

import ar.edu.unlu.poo.burako.modelo.Burako;
import ar.edu.unlu.poo.burako.modelo.Eventos;
import ar.edu.unlu.rmimvc.observer.IObservableRemoto;
import ar.edu.unlu.rmimvc.observer.IObservadorRemoto;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Observador encargado de persistir los resultados cuando una partida
 * finaliza.
 *
 * Al recibir el evento de finalización actualiza las estadísticas de los
 * jugadores, registra la información correspondiente en el ranking y
 * elimina el guardado temporal de la partida, si existe.
 */
public class ObservadorPersistencia implements IObservadorRemoto {

    private final PersistenciaService servicio;
    private final Burako estado;
    private final List<Usuario> usuarios;
    private final String idPartidaGuardada;

    public ObservadorPersistencia(PersistenciaService servicio, Burako estado,
                                   List<Usuario> usuarios, String idPartidaGuardada) {
        this.servicio = servicio;
        this.estado = estado;
        this.usuarios = usuarios;
        this.idPartidaGuardada = idPartidaGuardada;
    }

    @Override
    public void actualizar(IObservableRemoto observable, Object o) throws RemoteException {
        if (o == Eventos.partida_terminada) {
            servicio.finalizarPartida(estado, usuarios, idPartidaGuardada);
        }
    }
}
