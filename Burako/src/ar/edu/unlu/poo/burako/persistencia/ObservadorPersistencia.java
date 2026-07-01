package ar.edu.unlu.poo.burako.persistencia;

import ar.edu.unlu.poo.burako.modelo.Burako;
import ar.edu.unlu.poo.burako.modelo.Eventos;
import ar.edu.unlu.poo.burako.modelo.Observador;

/**
 * Observador adicional, registrado junto al Controlador, que reacciona
 * exclusivamente al evento {@code partida_terminada} para persistir el
 * resultado final: actualiza las estadísticas de ambos {@link Usuario}, el
 * Ranking, y elimina el guardado intermedio de la partida (si existía).
 *
 * NO modifica ni reemplaza el Controlador ni el contrato Observador/Observado
 * existente: es una nueva implementación que se agrega a la lista de
 * observadores que Burako ya admite (agregarObservador soporta múltiples
 * observadores desde su diseño original). Así, la persistencia automática
 * de resultados queda completamente desacoplada de la capa de Vista.
 */
public class ObservadorPersistencia implements Observador {

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
    public void notificar(Eventos evento) {
        if (evento == Eventos.partida_terminada) {
            servicio.finalizarPartida(estado, usuario1, usuario2, idPartidaGuardada);
        }
    }
}
