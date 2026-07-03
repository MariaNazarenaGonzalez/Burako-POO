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
 * Menú principal de la aplicación CLIENTE, completamente gráfico (Swing).
 * Es la primera ventana que ve cada jugador, antes de conectarse al
 * servidor. Ofrece unirse como Jugador 1, como Jugador 2, o salir.
 *
 * MODIFICADO (Fase 9 - Integración RMIMVC):
 * - Ya NO crea el modelo (Burako) ni depende de PersistenciaService: el
 *   servidor (ver servidor.AppServidor) es el único propietario del modelo
 *   y de la persistencia ("Los clientes únicamente poseen: Vista,
 *   Controlador, Proxy remoto"). Las opciones "Cargar partida" y
 *   "Ver ranking" de la Fase 7 se retiraron de este menú porque ambas
 *   requerían acceso directo a archivos, algo que un cliente ya no puede
 *   hacer; esa decisión de diseño se documenta en la entrega de esta fase.
 * - Cada proceso cliente representa a UN jugador (una Vista + un
 *   Controlador + un proxy remoto), tal como exige la arquitectura
 *   Cliente/Servidor solicitada: no puede haber un cliente que controle a
 *   los dos jugadores a la vez, porque eso implicaría acceso directo a un
 *   modelo que ya no reside en este proceso.
 * - "Unirse como Jugador 1/2" pide IP/puerto propios y del servidor,
 *   crea un Controlador (sin argumentos) y usa Cliente.iniciar(controlador)
 *   de la librería para conectar, obtener el stub remoto del modelo y
 *   registrarse automáticamente como observador remoto.
 * - preguntarTipoVista() y crearVista() se conservan textualmente iguales
 *   a los de la Fase 7 (misma firma, mismo comportamiento): la Vista
 *   (VistaGrafica/VistaConsola) no sufre ningún cambio.
 */
public class MenuPrincipal extends JFrame {

    public MenuPrincipal() {
        super("Burako - Cliente");
        construirVentana();
    }

    private void construirVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton botonJugador1 = new JButton("Unirse como Jugador 1");
        JButton botonJugador2 = new JButton("Unirse como Jugador 2");
        JButton botonSalir = new JButton("Salir");

        botonJugador1.addActionListener(e -> flujoConectar(0));
        botonJugador2.addActionListener(e -> flujoConectar(1));
        botonSalir.addActionListener(e -> System.exit(0));

        JPanel contenido = new JPanel(new GridLayout(3, 1, 12, 12));
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));
        contenido.add(botonJugador1);
        contenido.add(botonJugador2);
        contenido.add(botonSalir);
        setContentPane(contenido);

        setSize(340, 220);
        setLocationRelativeTo(null);
    }

    // ── Conexión al servidor (RMIMVC) ───────────────────────────────────────

    private void flujoConectar(int indiceJugador) {
        String ipCliente = pedirIpPropia();
        if (ipCliente == null) return;
        int puertoCliente = pedirPuerto("Puerto en el que escuchará este cliente",
                indiceJugador == 0 ? 9001 : 9002);
        if (puertoCliente < 0) return;

        String ipServidor = JOptionPane.showInputDialog(this, "IP del servidor:", "Conexión con el servidor",
                JOptionPane.QUESTION_MESSAGE);
        if (ipServidor == null || ipServidor.isBlank()) return;
        int puertoServidor = pedirPuerto("Puerto del servidor", 8888);
        if (puertoServidor < 0) return;

        boolean usarConsola = preguntarTipoVista();

        Controlador controlador = new Controlador();
        Cliente cliente = new Cliente(ipCliente, puertoCliente, ipServidor, puertoServidor);
        try {
            cliente.iniciar(controlador);
        } catch (RemoteException | RMIMVCException e) {
            JOptionPane.showMessageDialog(this, "No se pudo conectar con el servidor:\n" + e.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        crearVista(controlador, indiceJugador, usarConsola);
        setVisible(false);
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

    // ── Construcción de la Vista (idéntico a la Fase 7) ─────────────────────

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
