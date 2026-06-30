package ar.edu.unlu.poo.burako.vista;

import ar.edu.unlu.poo.burako.controlador.Controlador;
import ar.edu.unlu.poo.burako.modelo.Eventos;
import ar.edu.unlu.poo.burako.modelo.FichaMostrable;
import ar.edu.unlu.poo.burako.modelo.JuegoMostrable;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Vista consola del juego Burako.
 *
 * CORRECCIÓN (Fase 5 → compilación):
 * Los campos de UI (panelPrincipal, txtSalida, etc.) eran inicializados por el
 * método generado por IntelliJ GUI Designer a partir del archivo .form.
 * Al reescribir la clase programáticamente ese método desapareció, y
 * setContentPane(panelPrincipal) recibía null.
 * Se agrega crearUI() que construye los componentes antes de setContentPane.
 * El layout replica fielmente el definido en VistaConsola.form.
 *
 * INVARIANTES de las fases anteriores: intactos.
 * - No contiene lógica de dominio.
 * - No valida reglas del juego.
 * - El turno se consulta al controlador, no se calcula aquí.
 */
public class VistaConsola extends JFrame implements VistaJuego {

    private final Controlador controlador;
    private final int         miTurno;

    // ── Componentes UI (instanciados en crearUI()) ────────────────────────────
    private JPanel      panelPrincipal;
    private JTextPane   txtSalida;
    private JTextField  txtEntrada;
    private JButton     btnEnter;
    private JScrollPane scroll;

    // ── Estado de la máquina de estados de la consola ─────────────────────────
    private EstadoVistaConsola estado;
    private int[]              listafichas = {};
    private int                f;
    private int                j;

    public VistaConsola(Controlador controlador, int turno) {
        this.controlador = controlador;
        this.miTurno     = turno;

        // crearUI() DEBE preceder a setContentPane; construye panelPrincipal.
        crearUI();

        setTitle("Burako - " + controlador.getNombre(turno));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);
        setContentPane(panelPrincipal);

        conectarAcciones();
        actualizarMenuSegunTurno();
    }

    // ── Construcción de la UI (reemplaza el .form de IntelliJ) ───────────────

    /**
     * Construye la interfaz programáticamente.
     * Layout equivalente al definido en VistaConsola.form:
     *   - GridLayout 2 filas × 1 columna con margen
     *   - Fila 0: scroll con JTextPane (salida de texto coloreado)
     *   - Fila 1: campo de texto + botón Enter
     */
    private void crearUI() {
        panelPrincipal = new JPanel(new BorderLayout(4, 4));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Área de salida
        txtSalida = new JTextPane();
        txtSalida.setEditable(false);
        txtSalida.setFont(new Font("Monospaced", Font.BOLD, 14));
        txtSalida.setForeground(new Color(0xD96443)); // color original del .form

        scroll = new JScrollPane(txtSalida);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panelPrincipal.add(scroll, BorderLayout.CENTER);

        // Panel inferior: entrada + botón
        JPanel panelEntrada = new JPanel(new BorderLayout(4, 0));
        txtEntrada = new JTextField();
        txtEntrada.setFont(new Font("Monospaced", Font.BOLD, 14));
        btnEnter = new JButton("Enter");

        panelEntrada.add(txtEntrada, BorderLayout.CENTER);
        panelEntrada.add(btnEnter,   BorderLayout.EAST);
        panelPrincipal.add(panelEntrada, BorderLayout.SOUTH);
    }

    private void conectarAcciones() {
        btnEnter.addActionListener(e -> {
            procesarEntrada(txtEntrada.getText());
            txtEntrada.setText("");
        });

        // Enter en el campo de texto dispara el botón
        txtEntrada.addActionListener(e -> btnEnter.doClick());

        // Enter global en la ventana también lo dispara
        btnEnter.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ENTER"), "buttonPressed");
        btnEnter.getActionMap().put("buttonPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnEnter.doClick();
            }
        });
    }

    // ── Entrada del usuario ───────────────────────────────────────────────────

    private void procesarEntrada(String entrada) {
        switch (estado) {
            case MENU_CambioTurno:     procesarMenu_CambioTurno(entrada); break;
            case MENU_CambioTurnoOtro: procesarEspera_CambioTurno();      break;
            case MENU_Jugada:          procesarMenu_Jugada(entrada);       break;
            case BajarJuego:           procesarBajarJuego(entrada);        break;
            case ApoyarJuego_Juego:
            case ApoyarJuego_Ficha:
            case ApoyarJuego_Pos:      procesarApoyarJuego(entrada);       break;
            case AgregarPozo:          procesarAgregarPozo(entrada);       break;
        }
    }

    private void procesarEspera_CambioTurno() {
        println("espera " + getNombre(controlador.getTurnoActual()) + " esta jugando");
    }

    private void procesarMenu_CambioTurno(String entrada) {
        println(entrada);
        int turnoActual = controlador.getTurnoActual();
        switch (entrada) {
            case "1":
                if (controlador.agarrarMazo(turnoActual)) {
                    println("Agarraste una ficha del mazo.\nTú atril:\n");
                    mostrarAtril(miTurno);
                    print("\n");
                    mostrarMenu_Jugada();
                } else {
                    println("NO FUE POSIBLE AGARRAR UNA FICHA DEL MAZO");
                    println(controlador.getUltimoMensajeError());
                }
                break;
            case "2":
                if (controlador.agarrarPozo(turnoActual)) {
                    println("Agarraste una ficha del pozo.\nTú atril:\n");
                    mostrarAtril(miTurno);
                    print("\n");
                    mostrarMenu_Jugada();
                } else {
                    println("NO FUE POSIBLE AGARRAR FICHAS DEL POZO");
                    println(controlador.getUltimoMensajeError());
                }
                break;
            default:
                println("Opción no válida. Por favor, elija una opción válida.");
        }
    }

    private void procesarMenu_Jugada(String entrada) {
        println(entrada);
        switch (entrada) {
            case "1":
                println("Bajar un Juego");
                mostraBajarJuego();
                break;
            case "2":
                println("Apoyar un juego de la Mesa");
                mostrarApoyarJuego();
                break;
            case "3":
                println("Ceder el turno/Poner una Ficha en el pozo");
                mostrarAgregarPozo();
                break;
            default:
                println("Opción no válida. Por favor, elija una opción válida.");
        }
    }

    private void procesarBajarJuego(String entrada) {
        if (esEnteroPositivo(entrada)) {
            print(entrada + ", ");
            int[] nuevoArray = new int[listafichas.length + 1];
            System.arraycopy(listafichas, 0, nuevoArray, 0, listafichas.length);
            nuevoArray[listafichas.length] = Integer.parseInt(entrada);
            listafichas = nuevoArray;
        } else if (entrada.contentEquals(".")) {
            println(entrada);
            controlador.bajarJuego(controlador.getTurnoActual(), listafichas);
            listafichas = new int[0];
            mostrarMenu_Jugada();
        }
    }

    private void procesarApoyarJuego(String entrada) {
        println(entrada);
        switch (estado) {
            case ApoyarJuego_Juego:
                if (esEnteroPositivo(entrada)) {
                    print("eliga una Ficha:");
                    j = Integer.parseInt(entrada);
                    estado = EstadoVistaConsola.ApoyarJuego_Ficha;
                } else {
                    println("opcion invalida");
                }
                break;
            case ApoyarJuego_Ficha:
                if (esEnteroPositivo(entrada)) {
                    print("eliga la posicion en la que lo desea agregar:");
                    f = Integer.parseInt(entrada);
                    estado = EstadoVistaConsola.ApoyarJuego_Pos;
                } else {
                    println("opcion invalida");
                }
                break;
            case ApoyarJuego_Pos:
                if (esEnteroPositivo(entrada)) {
                    int p = Integer.parseInt(entrada);
                    controlador.apoyarJuego(f, p, controlador.getTurnoActual(), j);
                    mostrarMenu_Jugada();
                } else {
                    println("opcion invalida");
                }
                break;
        }
    }

    private void procesarAgregarPozo(String entrada) {
        if (esEnteroPositivo(entrada)) {
            println(entrada);
            controlador.agregarPozo(Integer.parseInt(entrada), controlador.getTurnoActual());
        } else {
            print("\n entrada invalida _vuelve a intentar_:");
        }
    }

    // ── Menús de presentación ─────────────────────────────────────────────────

    private void actualizarMenuSegunTurno() {
        if (controlador.getTurnoActual() == miTurno) {
            estado = EstadoVistaConsola.MENU_CambioTurno;
            println("Es turno de " + getNombre(miTurno));
            mostrarMesa();
            print("opciones:\n" +
                  "1- Agarrar una ficha del mazo\n" +
                  "2- Agarrar todo el pozo\n" +
                  "¿Qué quieres hacer?:");
        } else {
            estado = EstadoVistaConsola.MENU_CambioTurnoOtro;
            println("Es turno de " + getNombre(controlador.getTurnoActual()));
            println("Esta Jugando...");
        }
    }

    private void mostrarMenu_Jugada() {
        estado = EstadoVistaConsola.MENU_Jugada;
        print("opciones:\n" +
              "1- Bajar un Juego\n" +
              "2- Apoyar un juego de la Mesa\n" +
              "3- Ceder el turno/Poner una Ficha en el pozo\n" +
              "¿Qué quieres hacer?:");
    }

    private void mostraBajarJuego() {
        estado = EstadoVistaConsola.BajarJuego;
        mostrarMesa();
        print("Para seleccionar fichas indique su numero de indice \n" +
              "separado por ENTER, indique fin de selección con . :");
    }

    private void mostrarApoyarJuego() {
        estado = EstadoVistaConsola.ApoyarJuego_Juego;
        mostrarMesa();
        print("elige un juego:");
    }

    private void mostrarAgregarPozo() {
        estado = EstadoVistaConsola.AgregarPozo;
        mostrarMesa();
        print("elige una ficha:");
    }

    // ── VistaJuego ────────────────────────────────────────────────────────────

    @Override
    public void mostrarMesa() {
        int rival = (miTurno + 1) % 2;
        println("\tLa mesa se ve así:\nEl lado de " + getNombre(rival));
        mostrarJuegos(rival);
        print("\n");
        println("El lado de " + getNombre(miTurno) + " (tú)");
        mostrarJuegos(miTurno);
        print("\n");
        println("El pozo se ve así:");
        mostrarPozo();
        print("\n");
        println("Tú atril:");
        mostrarAtril(miTurno);
        print("\n");
        println("Turno actual: " + getNombre(controlador.getTurnoActual()));
    }

    @Override
    public void mesaje(Eventos eventos) {
        println(eventos.name());
        if (eventos == Eventos.agregarPozo_exitoso
                || eventos == Eventos.tomarMuerto_exitoso
                || eventos == Eventos.cortar_exitoso
                || eventos == Eventos.partida_terminada) {
            actualizarMenuSegunTurno();
        }
        if (eventos.name().endsWith("_NO_exitoso")) {
            println(controlador.getUltimoMensajeError());
        }
    }

    // ── Renderizado ───────────────────────────────────────────────────────────

    private void mostrarAtril(int jugador) {
        List<FichaMostrable> listaAtril = controlador.getAtril(jugador);
        int i = 1;
        for (FichaMostrable f : listaAtril) {
            print(i + "-[");
            printColor(f.getColor().name(), f.getColor().name());
            print("_");
            printColor(f.getNum().name(), f.getColor().name());
            print("]; ");
            i++;
        }
    }

    private void mostrarPozo() {
        List<FichaMostrable> listaPozo = controlador.getPozo();
        int i = 1;
        for (FichaMostrable f : listaPozo) {
            print(i + "-[");
            printColor(f.getColor().name(), f.getColor().name());
            print("_");
            printColor(f.getNum().name(), f.getColor().name());
            print("]; ");
            i++;
        }
    }

    private void mostrarJuegos(int jugador) {
        int i = 1;
        for (JuegoMostrable j : controlador.getJuegos(jugador)) {
            print("   " + i + "-");
            mostrarJuego(j);
            print("\n");
            i++;
        }
    }

    public void mostrarJuego(JuegoMostrable j) {
        for (FichaMostrable f : j.getFichas()) {
            print("[");
            printColor(f.getColor().name(), f.getColor().name());
            print("_");
            printColor(f.getNum().name(), f.getColor().name());
            print("] ");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getNombre(int turno) {
        return controlador.getNombre(turno);
    }

    private boolean esEnteroPositivo(String entrada) {
        try {
            return Integer.parseInt(entrada) >= 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scroll.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    public void printColor(String texto, String color) {
        StyledDocument doc = txtSalida.getStyledDocument();
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        switch (color) {
            case "Rojo":     StyleConstants.setForeground(attrs, Color.RED);    break;
            case "Negro":    StyleConstants.setForeground(attrs, Color.BLACK);  break;
            case "Amarillo": StyleConstants.setForeground(attrs, Color.YELLOW); break;
            case "Azul":     StyleConstants.setForeground(attrs, Color.BLUE);   break;
            default:         StyleConstants.setForeground(attrs, Color.BLACK);
        }
        try {
            doc.insertString(doc.getLength(), texto, attrs);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        scrollToBottom();
    }

    public void print(String texto) {
        printColor(texto, "Negro");
    }

    private void println(String string) {
        print(string + "\n");
    }
}
