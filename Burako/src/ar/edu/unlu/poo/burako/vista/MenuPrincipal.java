package ar.edu.unlu.poo.burako.vista;

import ar.edu.unlu.poo.burako.controlador.Controlador;
import ar.edu.unlu.rmimvc.RMIMVCException;
import ar.edu.unlu.rmimvc.Util;
import ar.edu.unlu.rmimvc.cliente.Cliente;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Ventana principal de la aplicación cliente.
 *
 * Permite establecer la conexión con el servidor, seleccionar el jugador
 * que representará el cliente y crear la vista elegida para participar
 * en la partida.
 */
public class MenuPrincipal extends JFrame {

    public MenuPrincipal() {
        super("Burako - Cliente");
        construirVentana();
    }

    private void construirVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton botonConectar = new JButton("Conectar a una partida");
        JButton botonSalir = new JButton("Salir");

        botonConectar.addActionListener(e -> flujoConectar());
        botonSalir.addActionListener(e -> System.exit(0));

        JPanel contenido = new JPanel(new GridLayout(2, 1, 12, 12));
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));
        contenido.add(botonConectar);
        contenido.add(botonSalir);
        setContentPane(contenido);

        setSize(340, 180);
        setLocationRelativeTo(null);
    }

    // ── Conexión al servidor (RMIMVC) ───────────────────────────────────────

    private void flujoConectar() {
        String ipCliente = pedirIpPropia();
        if (ipCliente == null) return;
        int puertoCliente = pedirPuerto("Puerto en el que escuchará este cliente", 9001);
        if (puertoCliente < 0) return;

        String ipServidor = JOptionPane.showInputDialog(this, "IP del servidor:", "Conexión con el servidor",
                JOptionPane.QUESTION_MESSAGE);
        if (ipServidor == null || ipServidor.isBlank()) return;
        int puertoServidor = pedirPuerto("Puerto del servidor", 8888);
        if (puertoServidor < 0) return;

        Controlador controlador = new Controlador();
        Cliente cliente = new Cliente(ipCliente, puertoCliente, ipServidor, puertoServidor);
        try {
            cliente.iniciar(controlador);
        } catch (RemoteException | RMIMVCException e) {
            JOptionPane.showMessageDialog(this, "No se pudo conectar con el servidor:\n" + e.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Integer indiceJugador = pedirIndiceJugador(controlador);
        if (indiceJugador == null) return;

        boolean usarConsola = preguntarTipoVista();
        crearVista(controlador, indiceJugador, usarConsola);
        setVisible(false);
    }

    /**
     * Permite seleccionar el jugador que representará este cliente dentro
     * de la partida y devuelve su índice correspondiente.
     */
    private Integer pedirIndiceJugador(Controlador controlador) {
        int cantidad = controlador.getCantidadJugadores();
        String[] opciones = new String[cantidad];
        for (int i = 0; i < cantidad; i++) {
            opciones[i] = "Jugador " + (i + 1);
        }

        String seleccion = (String) JOptionPane.showInputDialog(this,
                "¿Qué jugador sos? (partida de " + cantidad + " jugadores)",
                "Selección de jugador", JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        if (seleccion == null) return null;

        for (int i = 0; i < opciones.length; i++) {
            if (opciones[i].equals(seleccion)) return i;
        }
        return null;
    }

    private String pedirIpPropia() {
        ArrayList<String> ips = Util.getIpDisponibles();
        return (String) JOptionPane.showInputDialog(this,
                "IP en la que escuchará este cliente:", "Conexión",
                JOptionPane.QUESTION_MESSAGE, null, ips.toArray(), ips.isEmpty() ? null : ips.get(0));
    }

    private int pedirPuerto(String titulo, int porDefecto) {
        String puerto = JOptionPane.showInputDialog(this, titulo + ":", String.valueOf(porDefecto));
        if (puerto == null) return -1;
        try {
            return Integer.parseInt(puerto.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Puerto inválido.", "Burako", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
    }

    // ── Construcción de la Vista ──────────────
    /**
     * Solicita el tipo de interfaz que utilizará el jugador.
     */
    private boolean preguntarTipoVista() {
        Object[] opciones = {"Swing", "Consola"};
        int seleccion = JOptionPane.showOptionDialog(
                this,
                "¿Qué tipo de vista usarás?",
                "Selección de vista",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );
        return seleccion == 1;
    }
    /**
     * Crea la vista seleccionada, la asocia al controlador y la muestra
     * al usuario.
     */
    private void crearVista(Controlador controlador, int jugador, boolean usarConsola) {
        if (usarConsola) {
            var vista = new VistaConsola(controlador, jugador);
            controlador.setVista(vista);
            vista.setVisible(true);
        } else {
            var vista = new VistaGrafica(controlador, jugador);
            controlador.setVista(vista);
            vista.setVisible(true);
        }
    }
}
