package ar.edu.unlu.poo.burako.vista;

import ar.edu.unlu.poo.burako.controlador.Controlador;
import ar.edu.unlu.poo.burako.modelo.EstadoTurno;
import ar.edu.unlu.poo.burako.modelo.Eventos;
import ar.edu.unlu.poo.burako.modelo.FichaMostrable;
import ar.edu.unlu.poo.burako.modelo.JuegoMostrable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Vista gráfica (Swing) del juego Burako.
 *
 * CORRECCIÓN (Fase 5 → compilación):
 * Los campos de UI eran inicializados por el método generado por IntelliJ
 * GUI Designer. Al reescribir la clase ese método desapareció y
 * setContentPane(panelPrincipal) recibía null.
 * Se agrega crearUI() que construye todos los componentes antes de setContentPane.
 * El layout replica fielmente el definido en VistaGrafica.form:
 *   - Fila 0: lblEstado (etiqueta de turno)
 *   - Fila 1: 4 columnas → juegos rival | mis juegos | pozo | atril
 *   - Fila 2: botones de acción + área de eventos
 *
 * INVARIANTES de las fases anteriores: intactos.
 * - No contiene lógica de dominio.
 * - No valida reglas del juego.
 */
public class VistaGrafica extends JFrame implements VistaJuego {

    private final Controlador controlador;
    private final int         miTurno;
    private final Set<Integer> indicesSeleccionados = new LinkedHashSet<>();

    // ── Componentes UI (instanciados en crearUI()) ────────────────────────────
    private JPanel              panelPrincipal;
    private JLabel              lblEstado;
    private JPanel              panelJuegosRival;
    private JPanel              panelMisJuegos;
    private JPanel              panelPozo;
    private JList<String>       listaAtril;
    private DefaultListModel<String> modeloAtril;
    private JLabel              lblAyudaAtril;
    private JButton             btnTomarMazo;
    private JButton             btnTomarPozo;
    private JButton             btnBajarJuego;
    private JButton             btnApoyarJuego;
    private JButton             btnAgregarPozo;
    private JTextArea           txtEventos;

    public VistaGrafica(Controlador controlador, int turno) {
        this.controlador = controlador;
        this.miTurno     = turno;

        // crearUI() DEBE preceder a setContentPane; construye panelPrincipal.
        crearUI();

        setTitle("Burako - " + controlador.getNombre(turno));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationByPlatform(true);
        setContentPane(panelPrincipal);

        conectarAcciones();
        mostrarMesa();
        appendEvento("Vista lista para " + controlador.getNombre(turno) + ".");
        actualizarEstadoVisual();
    }

    // ── Construcción de la UI (reemplaza el .form de IntelliJ) ───────────────

    /**
     * Construye la interfaz programáticamente.
     * Layout equivalente al definido en VistaGrafica.form:
     *   BorderLayout principal con:
     *   - NORTH: etiqueta de estado
     *   - CENTER: 4 paneles de juego (rival | propio | pozo | atril)
     *   - SOUTH: botones de acción + log de eventos
     */
    private void crearUI() {
        panelPrincipal = new JPanel(new BorderLayout(12, 12));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ── Fila 0: estado ────────────────────────────────────────────────────
        lblEstado = new JLabel("Estado");
        lblEstado.setFont(new Font("SansSerif", Font.BOLD, 16));
        panelPrincipal.add(lblEstado, BorderLayout.NORTH);

        // ── Fila 1: 4 columnas de juego ───────────────────────────────────────
        JPanel panelMesa = new JPanel(new GridLayout(1, 4, 12, 0));

        panelJuegosRival = crearPanelSeccion("Juegos del rival");
        panelMisJuegos   = crearPanelSeccion("Tus juegos");
        panelPozo        = crearPanelSeccion("Pozo");

        // Atril con JList y ayuda
        JPanel panelAtrilContenedor = new JPanel(new BorderLayout(0, 4));
        panelAtrilContenedor.setBorder(BorderFactory.createTitledBorder("Tu atril"));
        modeloAtril = new DefaultListModel<>();
        listaAtril  = new JList<>(modeloAtril);
        listaAtril.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaAtril.setVisibleRowCount(18);
        listaAtril.setCellRenderer(crearRendererAtril());
        lblAyudaAtril = new JLabel("Clic para marcar/desmarcar fichas.");
        lblAyudaAtril.setFont(new Font("SansSerif", Font.ITALIC, 11));
        panelAtrilContenedor.add(new JScrollPane(listaAtril), BorderLayout.CENTER);
        panelAtrilContenedor.add(lblAyudaAtril, BorderLayout.SOUTH);

        panelMesa.add(new JScrollPane(panelJuegosRival));
        panelMesa.add(new JScrollPane(panelMisJuegos));
        panelMesa.add(new JScrollPane(panelPozo));
        panelMesa.add(panelAtrilContenedor);
        panelPrincipal.add(panelMesa, BorderLayout.CENTER);

        // ── Fila 2: botones + log ─────────────────────────────────────────────
        JPanel panelInferior = new JPanel(new BorderLayout(12, 0));

        JPanel panelBotones = new JPanel(new GridLayout(5, 1, 0, 8));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        btnTomarMazo   = new JButton("Tomar del mazo");
        btnTomarPozo   = new JButton("Tomar del pozo");
        btnBajarJuego  = new JButton("Bajar juego");
        btnApoyarJuego = new JButton("Apoyar ficha");
        btnAgregarPozo = new JButton("Tirar al pozo");
        panelBotones.add(btnTomarMazo);
        panelBotones.add(btnTomarPozo);
        panelBotones.add(btnBajarJuego);
        panelBotones.add(btnApoyarJuego);
        panelBotones.add(btnAgregarPozo);

        txtEventos = new JTextArea(6, 40);
        txtEventos.setEditable(false);
        txtEventos.setLineWrap(true);
        txtEventos.setWrapStyleWord(true);

        panelInferior.add(panelBotones, BorderLayout.WEST);
        panelInferior.add(new JScrollPane(txtEventos), BorderLayout.CENTER);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        // Listener de selección del atril
        listaAtril.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int idx = listaAtril.locationToIndex(e.getPoint());
                if (idx >= 0) {
                    alternarSeleccionAtril(idx);
                    e.consume();
                }
            }
        });
    }

    private JPanel crearPanelSeccion(String titulo) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        return panel;
    }

    private ListCellRenderer<? super String> crearRendererAtril() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setText((indicesSeleccionados.contains(index) ? "[x] " : "[ ] ") + value);
                return label;
            }
        };
    }

    private void conectarAcciones() {
        btnTomarMazo.addActionListener(e   -> tomarDelMazo());
        btnTomarPozo.addActionListener(e   -> tomarDelPozo());
        btnBajarJuego.addActionListener(e  -> bajarJuegoSeleccionado());
        btnApoyarJuego.addActionListener(e -> apoyarFichaSeleccionada());
        btnAgregarPozo.addActionListener(e -> agregarFichaAlPozo());
    }

    // ── Acciones del usuario — solo traducen al controlador ───────────────────

    private void tomarDelMazo() {
        controlador.agarrarMazo(miTurno);
    }

    private void tomarDelPozo() {
        controlador.agarrarPozo(miTurno);
    }

    /** No valida cantidad mínima: el modelo rechazará con bajarJuego_NO_exitoso si es inválido. */
    private void bajarJuegoSeleccionado() {
        int[] seleccion = listaAtril.getSelectedIndices();
        if (seleccion.length == 0) {
            appendEvento("Selecciona fichas del atril para bajar un juego.");
            return;
        }
        int[] indices = new int[seleccion.length];
        for (int i = 0; i < seleccion.length; i++) {
            indices[i] = seleccion[i] + 1;
        }
        controlador.bajarJuego(miTurno, indices);
    }

    /** No valida si hay juegos disponibles: el modelo rechazará con apoyarJuego_NO_exitoso si es inválido. */
    private void apoyarFichaSeleccionada() {
        if (listaAtril.getSelectedIndices().length != 1) {
            appendEvento("Selecciona exactamente una ficha del atril.");
            return;
        }
        int fichaSeleccionada = listaAtril.getSelectedIndex();

        Integer juego = pedirEnteroPositivo("Numero de juego:");
        if (juego == null) return;

        Integer posicion = pedirEnteroPositivo("Posicion donde agregar la ficha:");
        if (posicion == null) return;

        controlador.apoyarJuego(fichaSeleccionada + 1, posicion, miTurno, juego);
    }

    private void agregarFichaAlPozo() {
        if (listaAtril.getSelectedIndices().length != 1) {
            appendEvento("Selecciona una sola ficha para tirar al pozo.");
            return;
        }
        controlador.agregarPozo(listaAtril.getSelectedIndex() + 1, miTurno);
    }

    /** Solo valida que sea entero positivo; la validación de dominio la hace el modelo. */
    private Integer pedirEnteroPositivo(String mensaje) {
        String valor = JOptionPane.showInputDialog(this, mensaje, "Burako",
                JOptionPane.QUESTION_MESSAGE);
        if (valor == null) return null;
        try {
            int numero = Integer.parseInt(valor);
            if (numero < 1) { appendEvento("El valor debe ser un número positivo."); return null; }
            return numero;
        } catch (NumberFormatException e) {
            appendEvento("Debes ingresar un numero.");
            return null;
        }
    }

    // ── VistaJuego ────────────────────────────────────────────────────────────

    @Override
    public void mostrarMesa() {
        renderizarJuegos(panelJuegosRival, controlador.getJuegos((miTurno + 1) % 2));
        renderizarJuegos(panelMisJuegos,   controlador.getJuegos(miTurno));
        renderizarFichas(panelPozo,        controlador.getPozo());
        renderizarAtril(controlador.getAtril(miTurno));
        actualizarEstadoVisual();
    }

    @Override
    public void mesaje(Eventos evento) {
        appendEvento(evento.name());
        if (evento.name().endsWith("_NO_exitoso")) {
            appendEvento(controlador.getUltimoMensajeError());
        }
    }

    // ── Renderizado ───────────────────────────────────────────────────────────

    private void renderizarJuegos(JPanel panel, List<JuegoMostrable> juegos) {
        panel.removeAll();
        if (juegos.isEmpty()) {
            panel.add(crearEtiqueta("Sin juegos"));
        } else {
            int n = 1;
            for (JuegoMostrable juego : juegos) {
                panel.add(crearEtiqueta(n + ": " + formatearJuego(juego)));
                n++;
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    private void renderizarFichas(JPanel panel, List<FichaMostrable> fichas) {
        panel.removeAll();
        if (fichas.isEmpty()) {
            panel.add(crearEtiqueta("Pozo vacio"));
        } else {
            StringBuilder sb = new StringBuilder();
            for (FichaMostrable f : fichas) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(formatearFicha(f));
            }
            panel.add(crearEtiqueta(sb.toString()));
        }
        panel.revalidate();
        panel.repaint();
    }

    private void renderizarAtril(List<FichaMostrable> atril) {
        indicesSeleccionados.clear();
        listaAtril.clearSelection();
        modeloAtril.clear();
        for (int i = 0; i < atril.size(); i++) {
            modeloAtril.addElement((i + 1) + " - " + formatearFicha(atril.get(i)));
        }
    }

    private void alternarSeleccionAtril(int indice) {
        if (indicesSeleccionados.contains(indice)) {
            indicesSeleccionados.remove(indice);
        } else {
            indicesSeleccionados.add(indice);
        }
        listaAtril.clearSelection();
        for (Integer i : indicesSeleccionados) {
            listaAtril.addSelectionInterval(i, i);
        }
        listaAtril.repaint();
    }

    private void actualizarEstadoVisual() {
        int         turnoActual  = controlador.getTurnoActual();
        EstadoTurno estadoTurno  = controlador.getEstadoTurno();
        String      nombreTurno  = controlador.getNombre(turnoActual);

        String descripcion;
        if (turnoActual != miTurno) {
            descripcion = "Esperando a " + nombreTurno;
        } else if (estadoTurno == EstadoTurno.TOMAR) {
            descripcion = "Tu turno: toma del mazo o del pozo";
        } else {
            descripcion = "Tu turno: baja, apoya o tira una ficha al pozo";
        }

        lblEstado.setText("Jugador: " + controlador.getNombre(miTurno)
                + "  |  Turno: " + nombreTurno
                + "  |  " + descripcion);

        boolean puedeTomar  = controlador.puedeTomar(miTurno);
        boolean puedeJugar  = controlador.puedeJugar(miTurno);
        btnTomarMazo.setEnabled(puedeTomar);
        btnTomarPozo.setEnabled(puedeTomar);
        btnBajarJuego.setEnabled(puedeJugar);
        btnApoyarJuego.setEnabled(puedeJugar);
        btnAgregarPozo.setEnabled(puedeJugar);
    }

    // ── Helpers de presentación ───────────────────────────────────────────────

    private JLabel crearEtiqueta(String texto) {
        JLabel label = new JLabel("<html><body style='width:200px'>" + texto + "</body></html>");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private String formatearJuego(JuegoMostrable juego) {
        StringBuilder sb = new StringBuilder();
        for (FichaMostrable f : juego.getFichas()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(formatearFicha(f));
        }
        return sb.toString();
    }

    private String formatearFicha(FichaMostrable f) {
        return "[" + f.getColor().name() + "_" + f.getNum().name() + "]";
    }

    private void appendEvento(String texto) {
        txtEventos.append(texto + "\n");
        txtEventos.setCaretPosition(txtEventos.getDocument().getLength());
    }
}
