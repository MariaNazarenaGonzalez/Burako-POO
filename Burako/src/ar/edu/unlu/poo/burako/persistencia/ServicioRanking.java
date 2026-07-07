package ar.edu.unlu.poo.burako.persistencia;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Implementación remota de {@link IServicioRanking}.
 * Delega en {@link PersistenciaService}, único que conoce el
 * {@link RepositorioRanking}. Se instancia y publica del lado del servidor.
 */
public class ServicioRanking implements IServicioRanking {

    private final PersistenciaService persistencia;

    public ServicioRanking(PersistenciaService persistencia) {
        this.persistencia = persistencia;
    }

    @Override
    public List<EntradaRanking> obtenerRanking() throws RemoteException {
        return persistencia.obtenerRanking();
    }
}