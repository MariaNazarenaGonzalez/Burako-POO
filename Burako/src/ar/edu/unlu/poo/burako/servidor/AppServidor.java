package ar.edu.unlu.poo.burako.servidor;

import ar.edu.unlu.poo.burako.modelo.Burako;
import ar.edu.unlu.poo.burako.modelo.EstadoTurno;
import ar.edu.unlu.poo.burako.persistencia.ObservadorPersistencia;
import ar.edu.unlu.poo.burako.persistencia.PartidaGuardada;
import ar.edu.unlu.poo.burako.persistencia.PersistenciaService;
import ar.edu.unlu.poo.burako.persistencia.Usuario;
import ar.edu.unlu.rmimvc.RMIMVCException;
import ar.edu.unlu.rmimvc.Util;
import ar.edu.unlu.rmimvc.servidor.Servidor;

import javax.swing.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Punto de entrada de la aplicación servidor.
 *
 * Esta clase inicializa la partida, administra la persistencia de los
 * datos y publica el modelo para que pueda ser utilizado por los
 * clientes mediante RMIMVC.
 *
 * El servidor es responsable de crear o recuperar una partida,
 * registrar los observadores necesarios y mantener el estado del
 * juego durante toda su ejecución.
 */
public class AppServidor {

    public static void main(String[] args) {
        String ip = pedirIp();
        if (ip == null) return;
        int puerto = pedirPuerto("Puerto del servidor", 8888);
        if (puerto < 0) return;

        int cantidadJugadores = pedirCantidadJugadores();
        if (cantidadJugadores < 0) return;

        PersistenciaService persistencia = new PersistenciaService("data");

        List<Usuario> usuarios = obtenerUsuarios(persistencia, cantidadJugadores);
        if (usuarios == null) return;

        PartidaGuardada partidaPrevia = buscarPartidaAContinuar(persistencia, usuarios);

        Burako burako;
        String idPartida;
        try {
            if (partidaPrevia != null) {
                burako = partidaPrevia.getEstado();
                idPartida = partidaPrevia.getId();
            } else {
                burako = new Burako(cantidadJugadores);
                burako.setNombres(usuarios.stream().map(Usuario::getNombre).collect(Collectors.toList()));
                idPartida = UUID.randomUUID().toString();
            }
            burako.agregarObservador(new ObservadorPersistencia(persistencia, burako, usuarios, idPartida));
        } catch (RemoteException e) {
            // La interfaz declara RemoteException, por lo que la excepción
            // debe manejarse aunque la llamada sea local.
            JOptionPane.showMessageDialog(null, "Error inicializando la partida:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        registrarGuardadoAlCerrar(persistencia, burako, usuarios, idPartida);

        Servidor servidor = new Servidor(ip, puerto);
        try {
            servidor.iniciar(burako);
            String nombresJoin = String.join(" / ", usuarios.stream().map(Usuario::getNombre).collect(Collectors.toList()));
            JOptionPane.showMessageDialog(null,
                    "Servidor iniciado en " + ip + ":" + puerto + "\nPartida (" + cantidadJugadores
                            + " jugadores): " + nombresJoin,
                    "Burako - Servidor", JOptionPane.INFORMATION_MESSAGE);
        } catch (RemoteException | RMIMVCException e) {
            JOptionPane.showMessageDialog(null, "No se pudo iniciar el servidor:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Métodos relacionados con la persistencia de la partida.
    /**
     * Solicita la cantidad de jugadores que participarán
     * en la partida.
     *
     * @return cantidad de jugadores o -1 si la operación
     *         fue cancelada.
     */
    private static int pedirCantidadJugadores() {
        Object[] opciones = {"2 Jugadores", "4 Jugadores (2 equipos de 2)"};
        int seleccion = JOptionPane.showOptionDialog(null,
                "¿Cuántos jugadores participarán?", "Burako - Servidor",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        if (seleccion == JOptionPane.CLOSED_OPTION) return -1;
        return seleccion == 1 ? 4 : 2;
    }
    /**
     * Obtiene la información de los jugadores que participarán
     * en la partida.
     *
     * @param persistencia servicio encargado de administrar
     *                     los usuarios registrados.
     * @param cantidadJugadores cantidad de jugadores.
     * @return lista de usuarios o {@code null} si el proceso
     *         fue cancelado.
     */
    private static List<Usuario> obtenerUsuarios(PersistenciaService persistencia, int cantidadJugadores) {
        List<Usuario> usuarios = new ArrayList<>();
        for (int i = 1; i <= cantidadJugadores; i++) {
            Usuario usuario = obtenerUsuario(persistencia, "Jugador " + i);
            if (usuario == null) return null; // cancelado
            usuarios.add(usuario);
        }
        return usuarios;
    }
    /**
     * Solicita el nombre de un jugador y obtiene su registro
     * desde el sistema de persistencia.
     *
     * Si el usuario no existe previamente, se crea un nuevo
     * registro.
     *
     * @param persistencia servicio de persistencia.
     * @param etiqueta texto identificador del jugador.
     * @return usuario correspondiente o {@code null} si la
     *         operación fue cancelada.
     */
    private static Usuario obtenerUsuario(PersistenciaService persistencia, String etiqueta) {
        String nombre = JOptionPane.showInputDialog(null, "Nombre de " + etiqueta + ":", "Burako - Servidor",
                JOptionPane.QUESTION_MESSAGE);
        if (nombre == null) return null;
        nombre = nombre.trim();
        if (nombre.isEmpty()) nombre = etiqueta;
        return persistencia.obtenerOcrearUsuario(nombre);
    }

    /**
     * Busca una partida previamente guardada cuyos participantes
     * coincidan con los jugadores seleccionados.
     *
     * Si existe más de una partida compatible, se propone
     * continuar la más reciente.
     *
     * @param persistencia servicio de persistencia.
     * @param usuarios jugadores de la nueva partida.
     * @return partida recuperada o {@code null} si no existe
     *         una compatible o el usuario decide iniciar una
     *         partida nueva.
     */
    private static PartidaGuardada buscarPartidaAContinuar(PersistenciaService persistencia, List<Usuario> usuarios) {
        List<PartidaGuardada> guardadas = persistencia.listarPartidasDe(usuarios.get(0));
        for (int i = 1; i < usuarios.size(); i++) {
            String id = usuarios.get(i).getId();
            guardadas.removeIf(p -> !p.participaUsuario(id));
        }
        if (guardadas.isEmpty()) return null;

        PartidaGuardada ultima = guardadas.get(guardadas.size() - 1);
        String nombres = String.join(", ", ultima.getNombresUsuarios());
        int opcion = JOptionPane.showConfirmDialog(null,
                "Hay una partida guardada entre " + nombres + " (" + ultima.getFechaGuardado() + ").\n¿Continuarla?",
                "Burako - Servidor", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return opcion == JOptionPane.YES_OPTION ? ultima : null;
    }
    /**
     * Registra una tarea que guarda automáticamente la partida
     * cuando el servidor finaliza su ejecución.
     *
     * La partida solo se almacena si aún no ha terminado.
     *
     * @param persistencia servicio de persistencia.
     * @param burako modelo de la partida.
     * @param usuarios jugadores participantes.
     * @param idPartida identificador único de la partida.
     */
    private static void registrarGuardadoAlCerrar(PersistenciaService persistencia, Burako burako,
                                                    List<Usuario> usuarios, String idPartida) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (burako.getEstadoTurno() != EstadoTurno.PARTIDA_TERMINADA) {
                    persistencia.guardarPartida(idPartida, burako, usuarios);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }));
    }

    // ── Diálogos de conexión (mismo patrón que usa la librería en sus ejemplos) ─
    /**
     * Permite seleccionar la dirección IP sobre la cual
     * escuchará el servidor.
     *
     * @return dirección IP seleccionada o {@code null}
     *         si se cancela la operación.
     */
    private static String pedirIp() {
        ArrayList<String> ips = Util.getIpDisponibles();
        return (String) JOptionPane.showInputDialog(null,
                "Seleccione la IP en la que escuchará el servidor:", "IP del servidor",
                JOptionPane.QUESTION_MESSAGE, null, ips.toArray(), ips.isEmpty() ? null : ips.get(0));
    }
    /**
     * Solicita el puerto que utilizará el servidor.
     *
     * @param titulo título del cuadro de diálogo.
     * @param porDefecto puerto sugerido.
     * @return número de puerto o -1 si el valor ingresado
     *         es inválido o la operación fue cancelada.
     */
    private static int pedirPuerto(String titulo, int porDefecto) {
        String puerto = JOptionPane.showInputDialog(null, titulo + ":", String.valueOf(porDefecto));
        if (puerto == null) return -1;
        try {
            return Integer.parseInt(puerto.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Puerto inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }
}
