package ar.edu.unlu.poo.burako.controlador;

import ar.edu.unlu.poo.burako.modelo.*;
import ar.edu.unlu.poo.burako.vista.VistaJuego;
import ar.edu.unlu.rmimvc.cliente.IControladorRemoto;
import ar.edu.unlu.rmimvc.observer.IObservableRemoto;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

/**
 * Controlador: intermediario entre Modelo y Vista.
 *
 * MODIFICADO (Fase 4):
 * - El campo y constructor ahora dependen de IBurako (no de Burako concreto).
 *   Principio de Inversión de Dependencias: el controlador desconoce la implementación.
 * - Se eliminó notificar(Eventos, Exception): corresponde a la firma antigua.
 *   Los mensajes de error se obtienen con burako.getUltimoMensajeError().
 * - Se eliminaron getJuegos(), getAtril(), getNombre(), cantJuegos() que accedían
 *   a burako.getJugador() — método concreto ausente en IBurako.
 *   Reemplazados por llamadas directas a los métodos de IBurako.
 *
 * MODIFICADO (Fase 9 - Integración RMIMVC):
 * - Implementa IControladorRemoto (librería RMIMVC de la cátedra) en lugar
 *   de nuestra interfaz local Observador. IControladorRemoto extiende
 *   IObservadorRemoto y agrega setModeloRemoto(), tal como indica el README
 *   de la librería: "Hacer que el controlador implemente IControladorRemoto.
 *   Cambiar el método de notificación por actualizar(). Implementar
 *   setModeloRemoto() guardando el modelo remoto".
 * - notificar(Eventos) se renombra a actualizar(IObservableRemoto, Object)
 *   por exigencia de la interfaz de la librería; el CUERPO del método
 *   (la lógica de qué hacer ante una notificación) es exactamente el mismo
 *   que antes, solo cambia la firma y se castea el Object recibido a Eventos.
 * - El campo burako ya no es final: se asigna en setModeloRemoto() cuando
 *   el Cliente RMIMVC conecta con el servidor, no en el constructor.
 *   Se agrega un constructor sin argumentos (usado por AppCliente) además
 *   del constructor existente (se conserva para no romper compatibilidad).
 * - Ya no se llama manualmente a burako.agregarObservador(this): la
 *   librería registra automáticamente al controlador como observador
 *   remoto dentro de Cliente.iniciar(controlador).
 * - Cada llamada al modelo ahora puede lanzar RemoteException (la
 *   comunicación con el servidor puede fallar). Se captura DENTRO del
 *   Controlador para que la Vista (VistaGrafica/VistaConsola) no necesite
 *   ningún cambio: sus firmas de método siguen retornando exactamente los
 *   mismos tipos que antes, sin excepciones chequeadas nuevas.
 *
 * Invariantes (sin cambios):
 * - El controlador NO contiene reglas del juego.
 * - El controlador NO conoce implementaciones concretas del modelo.
 * - Toda actualización de la vista ocurre desde aquí, al recibir un evento.
 */
public class Controlador implements IControladorRemoto {

    private IBurako burako;
    private VistaJuego vista;

    public Controlador(IBurako burako) {
        this.burako = burako;
    }

    /** Constructor sin argumentos: usado por AppCliente antes de conectar con el servidor. */
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

    /** NUEVO (Fase 10 - Soporte 2 o 4 jugadores). */
    public void setNombres(List<String> nombres) {
        try {
            burako.setNombres(nombres);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** NUEVO (Fase 10 - Soporte 2 o 4 jugadores). */
    public int getCantidadJugadores() {
        try {
            return burako.getCantidadJugadores();
        } catch (RemoteException e) {
            e.printStackTrace();
            return 2;
        }
    }

    /** NUEVO (Fase 10 - Soporte 2 o 4 jugadores). */
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
