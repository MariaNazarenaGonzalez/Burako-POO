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
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Vista consola del juego Burako.
 *
 * REFACTORIZADO (análisis de controladores):
 *
 * ELIMINADO — lógica de negocio que no corresponde a la vista:
 * - Campo "turno" propio: la vista no administra el turno; lo consulta al modelo
 *   via controlador.getTurnoActual(). Tener un turno local duplicaba el estado
 *   y podía desincronizarse.
 * - mostrarMENU_CambioTurno() calculaba (turno+1)%2 para decidir si "es mi turno":
 *   esto es lógica de dominio. Ahora se consulta controlador.getTurnoActual().
 * - mostraBajarJuego() validaba getAtril(turno).size() >= 3: regla del juego.
 *   Eliminado; el modelo rechaza la jugada y notifica bajarJuego_NO_exitoso.
 * - mostrarApoyarJuego() validaba cantJuegos(turno) > 0: regla del juego.
 *   Eliminado; el modelo rechaza la jugada y notifica apoyarJuego_NO_exitoso.
 * - isNumero() validaba numero <= getAtril(turno).size(): validación de dominio.
 *   Ahora solo verifica que la entrada sea un entero positivo; el modelo valida
 *   si la posición existe en el atril.
 * - mesaje(Eventos) tenía un if(eventos==agregarPozo_exitoso) para avanzar el turno
 *   visual: lógica de flujo. Ahora mesaje() solo muestra; mostrarMesa() se encarga
 *   de reflejar el estado actual del modelo (incluido el turno nuevo).
 *
 * INVARIANTES de la vista:
 * - No toma decisiones de dominio.
 * - No valida reglas del juego.
 * - Solo traduce acciones del usuario al controlador y muestra lo que el modelo dice.
 */
public class VistaConsola extends JFrame implements VistaJuego {
    private final Controlador controlador;
    private JPanel panelPrincipal;
    private JTextPane txtSalida;
    private JTextField txtEntrada;
    private JButton btnEnter;
    private JScrollPane scroll;
    private EstadoVistaConsola estado;
    private final int miTurno;
    private int[] listafichas = {};
    private int f;
    private int j;

    public VistaConsola(Controlador controlador, int turno) {
        setTitle("Burako");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);
        setContentPane(panelPrincipal);
        this.controlador = controlador;
        this.miTurno = turno;
        this.txtSalida.setFont(new Font("Monospaced", Font.BOLD, 17));
        this.txtEntrada.setFont(new Font("Monospaced", Font.BOLD, 14));

        btnEnter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                procesarEntrada(txtEntrada.getText());
                txtEntrada.setText("");
            }
        });
        btnEnter.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ENTER"), "buttonPressed");
        btnEnter.getActionMap().put("buttonPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnEnter.doClick();
            }
        });

        // La vista consulta al modelo quién juega primero; no lo asume.
        actualizarMenuSegunTurno();
    }

    // ── Entrada del usuario ───────────────────────────────────────────────────

    private void procesarEntrada(String entrada) {
        switch (estado) {
            case MENU_CambioTurno:
                procesarMenu_CambioTurno(entrada);
                break;
            case MENU_CambioTurnoOtro:
                procesarEspera_CambioTurno();
                break;
            case MENU_Jugada:
                procesarMenu_Jugada(entrada);
                break;
            case BajarJuego:
                procesarBajarJuego(entrada);
                break;
            case ApoyarJuego_Juego:
            case ApoyarJuego_Ficha:
            case ApoyarJuego_Pos:
                procesarApoyarJuego(entrada);
                break;
            case AgregarPozo:
                procesarAgregarPozo(entrada);
                break;
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
                if (this.controlador.agarrarMazo(turnoActual)) {
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
                if (this.controlador.agarrarPozo(turnoActual)) {
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
            this.controlador.bajarJuego(controlador.getTurnoActual(), listafichas);
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
                    this.controlador.apoyarJuego(f, p, controlador.getTurnoActual(), j);
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
            int n = Integer.parseInt(entrada);
            this.controlador.agregarPozo(n, controlador.getTurnoActual());
        } else {
            print("\n entrada invalida _vuelve a intentar_:");
        }
    }

    // ── Menús de presentación ─────────────────────────────────────────────────

    /**
     * Decide qué menú mostrar según el turno actual del modelo.
     * La vista no calcula el turno; lo consulta.
     */
    private void actualizarMenuSegunTurno() {
        if (controlador.getTurnoActual() == miTurno) {
            estado = EstadoVistaConsola.MENU_CambioTurno;
            println("Es turno de " + getNombre(miTurno));
            mostrarMesa();
            print("\n" +
                "opciones:\n" +
                "1- Agarrar una ficha del mazo\n" +
                "2- Agarrar todo el pozo\n" +
                "¿Qué quieres hacer?:\n" +
                "");
        } else {
            estado = EstadoVistaConsola.MENU_CambioTurnoOtro;
            println("Es turno de " + getNombre(controlador.getTurnoActual()));
            println("Esta Jugando...");
        }
    }

    private void mostrarMenu_Jugada() {
        estado = EstadoVistaConsola.MENU_Jugada;
        print("\n" +
                "opciones:\n" +
                "1- Bajar un Juego\n" +
                "2- Apoyar un juego de la Mesa\n" +
                "3- Ceder el turno/Poner una Ficha en el pozo\n" +
                "¿Qué quieres hacer?:");
    }

    /**
     * La vista no valida si hay fichas suficientes para bajar (regla del modelo).
     * Muestra el menú y delega; el modelo rechazará con bajarJuego_NO_exitoso si no es válido.
     */
    private void mostraBajarJuego() {
        estado = EstadoVistaConsola.BajarJuego;
        mostrarMesa();
        print("Para seleccionar fichas indique su numero de indice \n" +
                "separado por ENTER, indique fin de selección con . :");
    }

    /**
     * La vista no valida si hay juegos disponibles para apoyar (regla del modelo).
     * Muestra el menú y delega; el modelo rechazará con apoyarJuego_NO_exitoso si no es válido.
     */
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
        int turnoActual = controlador.getTurnoActual();
        int rival = (miTurno + 1) % 2;
        println("\tLa mesa se ve así:\n" +
                "El lado de " + getNombre(rival));
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
        println("Turno actual: " + getNombre(turnoActual));
    }

    @Override
    public void mesaje(Eventos eventos) {
        println(eventos.name());
        // Al recibir cualquier evento, refrescar el menú según el estado
        // actual del modelo — sin calcular turnos ni anticipar flujo.
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
        List<JuegoMostrable> listaJuegos = controlador.getJuegos(jugador);
        for (JuegoMostrable j : listaJuegos) {
            print("   " + i + "-");
            mostrarJuego(j);
            print("\n");
            i++;
        }
    }

    public void mostrarJuego(JuegoMostrable j) {
        List<FichaMostrable> listaJuego = j.getJuego();
        for (FichaMostrable f : listaJuego) {
            print("[");
            printColor(f.getColor().name(), f.getColor().name());
            print("_");
            printColor(f.getNum().name(), f.getColor().name());
            print("] ");
        }
    }

    // ── Helpers de presentación ───────────────────────────────────────────────

    private String getNombre(int turno) {
        return this.controlador.getNombre(turno);
    }

    /**
     * Valida únicamente que la entrada sea un entero positivo.
     * Si la posición no existe en el atril, el modelo lo rechazará.
     */
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
            case "Rojo":    StyleConstants.setForeground(attrs, Color.RED);    break;
            case "Negro":   StyleConstants.setForeground(attrs, Color.BLACK);  break;
            case "Amarillo":StyleConstants.setForeground(attrs, Color.YELLOW); break;
            case "Azul":    StyleConstants.setForeground(attrs, Color.BLUE);   break;
            default:        StyleConstants.setForeground(attrs, Color.BLACK);
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
