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
 * Punto de entrada del SERVIDOR.
 *
 * NUEVO (Fase 9 - Integración RMIMVC).
 *
 * El servidor es el único propietario del modelo: crea (o recupera) la
 * única instancia de {@link Burako} que existirá durante toda la ejecución
 * ("Existe únicamente una partida"), la publica remotamente con
 * {@link Servidor}, y es el único proceso que toca el sistema de archivos
 * a través de {@link PersistenciaService}. Los clientes (ver
 * {@code vista.MenuPrincipal}) nunca acceden a persistencia directamente.
 *
 * Reutiliza 100% de la lógica de {@link PersistenciaService} ya existente
 * (registrar/recuperar Usuario, listar y continuar partidas guardadas,
 * ObservadorPersistencia, guardado al cerrar): es exactamente el mismo
 * flujo que antes ejecutaba vista.MenuPrincipal en el proceso local único,
 * ahora reubicado aquí porque es el servidor quien decide y posee la
 * partida. No se modificó ninguna clase de persistencia para lograrlo.
 *
 * MODIFICADO (Fase 10 - Soporte 2 o 4 jugadores):
 * - Antes de pedir nombres, se pregunta la cantidad de jugadores (2 o 4).
 * - obtenerUsuario/buscarPartidaAContinuar/registrarGuardadoAlCerrar ahora
 *   trabajan con List<Usuario> en lugar de exactamente 2 parámetros fijos,
 *   reutilizando los mismos overloads de PersistenciaService agregados
 *   para esta fase.
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
            // Llamada local dentro del propio proceso del servidor: no debería fallar por red,
            // pero se maneja igual porque IBurako/IObservableRemoto la declaran.
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

    // ── Persistencia (idéntica a la Fase 6/7, ahora ejecutada en el servidor) ──

    private static int pedirCantidadJugadores() {
        Object[] opciones = {"2 Jugadores", "4 Jugadores (2 equipos de 2)"};
        int seleccion = JOptionPane.showOptionDialog(null,
                "¿Cuántos jugadores participarán?", "Burako - Servidor",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        if (seleccion == JOptionPane.CLOSED_OPTION) return -1;
        return seleccion == 1 ? 4 : 2;
    }

    private static List<Usuario> obtenerUsuarios(PersistenciaService persistencia, int cantidadJugadores) {
        List<Usuario> usuarios = new ArrayList<>();
        for (int i = 1; i <= cantidadJugadores; i++) {
            Usuario usuario = obtenerUsuario(persistencia, "Jugador " + i);
            if (usuario == null) return null; // cancelado
            usuarios.add(usuario);
        }
        return usuarios;
    }

    private static Usuario obtenerUsuario(PersistenciaService persistencia, String etiqueta) {
        String nombre = JOptionPane.showInputDialog(null, "Nombre de " + etiqueta + ":", "Burako - Servidor",
                JOptionPane.QUESTION_MESSAGE);
        if (nombre == null) return null;
        nombre = nombre.trim();
        if (nombre.isEmpty()) nombre = etiqueta;
        return persistencia.obtenerOcrearUsuario(nombre);
    }

    /** Busca una partida guardada donde participen EXACTAMENTE los mismos usuarios (mismo conjunto). */
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

    private static String pedirIp() {
        ArrayList<String> ips = Util.getIpDisponibles();
        return (String) JOptionPane.showInputDialog(null,
                "Seleccione la IP en la que escuchará el servidor:", "IP del servidor",
                JOptionPane.QUESTION_MESSAGE, null, ips.toArray(), ips.isEmpty() ? null : ips.get(0));
    }

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
