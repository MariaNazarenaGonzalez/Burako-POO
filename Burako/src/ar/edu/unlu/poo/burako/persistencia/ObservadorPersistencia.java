package ar.edu.unlu.poo.burako.persistencia;

import ar.edu.unlu.poo.burako.modelo.Burako;
import ar.edu.unlu.poo.burako.modelo.Eventos;
import ar.edu.unlu.rmimvc.observer.IObservableRemoto;
import ar.edu.unlu.rmimvc.observer.IObservadorRemoto;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Observador adicional, registrado junto al Controlador, que reacciona
 * exclusivamente al evento {@code partida_terminada} para persistir el
 * resultado final: actualiza las estadísticas de todos los {@link Usuario}
 * participantes, el Ranking, y elimina el guardado intermedio de la
 * partida (si existía).
 *
 * NO modifica ni reemplaza al Controlador ni la lógica de persistencia:
 * es una implementación adicional que se agrega a la lista de observadores
 * que el modelo ya admite. Así, la persistencia automática de resultados
 * queda completamente desacoplada de la capa de Vista.
 *
 * IMPORTANTE: esta clase se instancia y se registra EXCLUSIVAMENTE en el
 * servidor (ver servidor.AppServidor), en el mismo proceso donde vive el
 * Burako real. Nunca se exporta como objeto remoto ni cruza la red: la
 * librería RMIMVC invoca actualizar() como una llamada Java local común
 * dentro de ObservableRemoto.notificarObservadores(), sin pasar por RMI.
 *
 * MODIFICADO (Fase 9 - Integración RMIMVC): implementa IObservadorRemoto en
 * lugar de nuestra interfaz local Observador (ver Burako.agregarObservador,
 * heredado de ObservableRemoto).
 *
 * MODIFICADO (Fase 10 - Soporte 2 o 4 jugadores): el constructor recibe una
 * List<Usuario> en lugar de exactamente 2, para funcionar igual con
 * partidas de 2 o de 4 jugadores sin duplicar la clase.
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
