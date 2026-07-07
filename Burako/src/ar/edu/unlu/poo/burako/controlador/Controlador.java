package ar.edu.unlu.poo.burako.controlador;

import ar.edu.unlu.poo.burako.modelo.*;
import ar.edu.unlu.poo.burako.vista.VistaJuego;
import ar.edu.unlu.rmimvc.cliente.IControladorRemoto;
import ar.edu.unlu.rmimvc.observer.IObservableRemoto;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

/**

* Actúa como intermediario entre el modelo y la vista.
*
* Recibe las acciones realizadas por el usuario, las delega al modelo y
* actualiza la vista cuando recibe notificaciones de cambios en el estado
* de la partida. También encapsula el manejo de errores de comunicación
* con el modelo remoto para evitar que la vista deba gestionarlos.
  */

public class Controlador implements IControladorRemoto {

    private IBurako burako;
    private VistaJuego vista;

    public Controlador(IBurako burako) {
        this.burako = burako;
    }

    /**
    * Crea un controlador sin un modelo asociado. El modelo se asigna
    * posteriormente cuando se establece la conexión con la partida.
    */

    public Controlador() {
    }

    // ── RMIMVC: Observer remoto ──────────────────────────────────────────────────

    @Override
    public void actualizar(IObservableRemoto observable, Object o) throws RemoteException {
        if (o instanceof Eventos) {
            Eventos evento = (Eventos) o;
            vista.mostrarMesa();
            vista.mesaje(evento);
        }
    }

    @Override
    public <T extends IObservableRemoto> void setModeloRemoto(T modeloRemoto) throws RemoteException {
        this.burako = (IBurako) modeloRemoto;
    }

    // ── Configuración ─────────────────────────────────────────────────────────

    public void setVista(VistaJuego vista) {
        this.vista = vista;
    }

    public void setNombres(String nombreJugador1, String nombreJugador2) {
        try {
            burako.setNombres(nombreJugador1, nombreJugador2);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Asigna los nombres de todos los jugadores de la partida. */
    public void setNombres(List<String> nombres) {
        try {
            burako.setNombres(nombres);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    /** Devuelve la cantidad de jugadores de la partida. */
    public int getCantidadJugadores() {
        try {
            return burako.getCantidadJugadores();
        } catch (RemoteException e) {
            e.printStackTrace();
            return 2;
        }
    }

    /** Devuelve el equipo al que pertenece el jugador indicado. */
    public int getEquipo(int indice) {
        try {
            return burako.getEquipo(indice);
        } catch (RemoteException e) {
            e.printStackTrace();
            return indice;
        }
    }

    // ── Consultas de estado ────────────────────────────────────────────────────

    public int getTurnoActual() {
        try {
            return burako.getTurnoActual();
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public EstadoTurno getEstadoTurno() {
        try {
            return burako.getEstadoTurno();
        } catch (RemoteException e) {
            e.printStackTrace();
            return EstadoTurno.PARTIDA_TERMINADA;
        }
    }

    public boolean puedeTomar(int turno) {
        try {
            return burako.puedeTomar(turno);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean puedeJugar(int turno) {
        try {
            return burako.puedeJugar(turno);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getNombre(int turno) {
        try {
            return burako.getNombreJugador(turno);
        } catch (RemoteException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getUltimoMensajeError() {
        try {
            return burako.getUltimoMensajeError();
        } catch (RemoteException e) {
            e.printStackTrace();
            return "Error de comunicación con el servidor.";
        }
    }

    // ── Consultas de datos de juego ────────────────────────────────────────────

    public List<JuegoMostrable> getJuegos(int jugador) {
        try {
            return burako.getJuegos(jugador);
        } catch (RemoteException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<FichaMostrable> getPozo() {
        try {
            return burako.getPozo();
        } catch (RemoteException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<FichaMostrable> getAtril(int turno) {
        try {
            return burako.getAtril(turno);
        } catch (RemoteException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public int cantJuegos(int turno) {
        try {
            return burako.cantJuegos(turno);
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<ResultadoJugador> getResultados() {
        try {
            return burako.getResultados();
        } catch (RemoteException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // ── Acciones del turno ─────────────────────────────────────────────────────

    public boolean agarrarPozo(int turno) {
        try {
            return burako.agarrarPozo(turno);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean agarrarMazo(int turno) {
        try {
            return burako.agarrarMazo(turno);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void bajarJuego(int turno, int[] listafichas) {
        try {
            burako.bajarJuego(turno, listafichas);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void agregarPozo(int n, int turno) {
        try {
            burako.agregarPozo(n, turno);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void apoyarJuego(int ficha, int pos, int turno, int juego) {
        try {
            burako.apoyarJuego(ficha, pos, turno, juego);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
