package ar.edu.unlu.poo.burako.persistencia;

import ar.edu.unlu.poo.burako.modelo.Burako;
import ar.edu.unlu.poo.burako.modelo.Eventos;
import ar.edu.unlu.rmimvc.observer.IObservableRemoto;
import ar.edu.unlu.rmimvc.observer.IObservadorRemoto;

import java.rmi.RemoteException;

/**
 * Observador adicional, registrado junto al Controlador, que reacciona
 * exclusivamente al evento {@code partida_terminada} para persistir el
 * resultado final: actualiza las estadísticas de ambos {@link Usuario}, el
 * Ranking, y elimina el guardado intermedio de la partida (si existía).
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
 * MODIFICADO (Fase 9 - Integración RMIMVC):
 * - Implementa IObservadorRemoto (librería RMIMVC) en lugar de nuestra
 *   interfaz local Observador, ya que Burako.agregarObservador() ahora
 *   proviene de ObservableRemoto y exige ese tipo. El método notificar(Eventos)
 *   se renombra a actualizar(IObservableRemoto, Object) por exigencia de la
 *   interfaz; la lógica interna (chequear si el evento es partida_terminada
 *   y delegar a PersistenciaService.finalizarPartida) es idéntica a la de
 *   fases anteriores.
 */
public class ObservadorPersistencia implements IObservadorRemoto {

    private final PersistenciaService servicio;
    private final Burako estado;
    private final Usuario usuario1;
    private final Usuario usuario2;
    private final String idPartidaGuardada;

    public ObservadorPersistencia(PersistenciaService servicio, Burako estado,
                                   Usuario usuario1, Usuario usuario2, String idPartidaGuardada) {
        this.servicio = servicio;
        this.estado = estado;
        this.usuario1 = usuario1;
        this.usuario2 = usuario2;
        this.idPartidaGuardada = idPartidaGuardada;
    }

    @Override
    public void actualizar(IObservableRemoto observable, Object o) throws RemoteException {
        if (o == Eventos.partida_terminada) {
            servicio.finalizarPartida(estado, usuario1, usuario2, idPartidaGuardada);
        }
    }
}
