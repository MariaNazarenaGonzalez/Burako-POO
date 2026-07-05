package ar.edu.unlu.poo.burako.vista;

import ar.edu.unlu.poo.burako.controlador.Controlador;
import ar.edu.unlu.poo.burako.modelo.EstadoTurno;
import ar.edu.unlu.poo.burako.modelo.Eventos;
import ar.edu.unlu.poo.burako.modelo.FichaMostrable;
import ar.edu.unlu.poo.burako.modelo.JuegoMostrable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class VistaGrafica extends JFrame implements VistaJuego {

    // ── Paleta ────────────────────────────────────────────────────────────────
    private static final Color C_MESA        = new Color(0x8B5A2B);
    private static final Color C_MESA_OSCURA = new Color(0x5C3317);
    private static final Color C_LATERAL     = new Color(0xF5F5F5);
    private static final Color C_FICHA_R     = new Color(0xCC2020);
    private static final Color C_FICHA_A     = new Color(0x1565C0);
    private static final Color C_FICHA_AM    = new Color(0xF9A825);
    private static final Color C_FICHA_N     = new Color(0x212121);
    private static final Color C_FICHA_FONDO = new Color(0xFFF9F0);
    // Selección: fondo cyan claro + borde rojo grueso (visible sobre cualquier color de ficha)
    private static final Color C_SELEC_FONDO = new Color(0xE0F7FA);
    private static final Color C_SELEC_BORDE = new Color(0xC62828);

    // ── Dimensiones ───────────────────────────────────────────────────────────
    private static final int FICHA_W = 48;
    private static final int FICHA_H = 62;
    private static final int FICHA_R = 9;
    private static final int FICHA_G = 6;

    // ── Dependencias ──────────────────────────────────────────────────────────
    private final Controlador  controlador;
    private final int          miTurno;
    private final Set<Integer> indicesSeleccionados = new LinkedHashSet<>();

    /**
     * MODIFICADO (Fase 10 - Soporte 2 o 4 jugadores): antes había un único
     * panelJuegosRival fijo, calculado como (miTurno+1)%2 (solo válido para
     * 2 jugadores). Ahora se construye UN panel por cada otro jugador de
     * la partida (1 con 2 jugadores, 3 con 4), guardado en panelesOtros.
     * indicesOtros[k] indica a qué jugador (0-based) corresponde
     * panelesOtros[k], en el mismo orden.
     */
    private JPanel[] panelesOtros;
    private int[]    indicesOtros;

    // ── Componentes ───────────────────────────────────────────────────────────
    private JLabel    lblEstado;
    private JPanel    panelMisJuegos;
    private JPanel    panelMazo;      // ficha dibujada — actúa de botón
    private JPanel    panelPozo;      // fichas del pozo
    private JPanel    panelAtril;
    private JButton   btnBajarJuego;
    private JButton   btnApoyarJuego;
    private JButton   btnAgregarPozo;
    private JButton   btnTomarMazo;   // accionado también por clic en panelMazo
    private JButton   btnTomarPozo;   // vive en columna derecha
    private JButton   btnMenu;
    private JTextArea txtEventos;

    private List<FichaMostrable> atrilActual = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────────────
    public VistaGrafica(Controlador controlador, int turno) {
        this.controlador = controlador;
        this.miTurno     = turno;

        int cantidad = controlador.getCantidadJugadores();
        indicesOtros = new int[cantidad - 1];
        int k = 0;
        for (int i = 0; i < cantidad; i++) {
            if (i != miTurno) indicesOtros[k++] = i;
        }

        crearUI();

        setTitle("Burako  —  " + controlador.getNombre(turno));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationByPlatform(true);

        conectarAcciones();
        mostrarMesa();
        appendEvento("Mesa lista para " + controlador.getNombre(turno) + ".");
        actualizarEstadoVisual();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI
    // ─────────────────────────────────────────────────────────────────────────

    private void crearUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_MESA);
        setContentPane(root);

        // Barra de estado
        lblEstado = new JLabel("Estado");
        lblEstado.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblEstado.setForeground(Color.WHITE);
        lblEstado.setOpaque(true);
        lblEstado.setBackground(new Color(0x37474F));
        lblEstado.setBorder(new EmptyBorder(7, 14, 7, 14));
        root.add(lblEstado, BorderLayout.NORTH);

        // Zona central
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(C_MESA);
        root.add(centro, BorderLayout.CENTER);

        centro.add(crearColumnaIzquierda(), BorderLayout.WEST);
        centro.add(crearTablero(),          BorderLayout.CENTER);
        centro.add(crearColumnaDerecha(),   BorderLayout.EAST);

        // Franja inferior
        root.add(crearFranjaInferior(), BorderLayout.SOUTH);
    }

    // ── Columna izquierda ─────────────────────────────────────────────────────
    // Contiene: ficha-mazo (clickable) + botones de acción
    // SIN botón duplicado de Mazo, SIN botón de Pozo (que va a la derecha)

    private JPanel crearColumnaIzquierda() {
        JPanel col = new JPanel(new BorderLayout(0, 12));
        col.setBackground(C_LATERAL);
        col.setPreferredSize(new Dimension(130, 0));   // más ancho → textos completos
        col.setBorder(new EmptyBorder(14, 10, 14, 10));

        // Ficha del mazo: panel dibujado que funciona como botón
        panelMazo = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujarFichaDorso(g, (getWidth() - FICHA_W) / 2, 6);
            }
        };
        panelMazo.setBackground(C_LATERAL);
        panelMazo.setPreferredSize(new Dimension(110, 85));
        panelMazo.setToolTipText("Clic para tomar del mazo");
        panelMazo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Botón invisible de mazo (solo para reutilizar la lógica de enable/disable)
        btnTomarMazo = new JButton();
        btnTomarMazo.setVisible(false);

        // Botones de acción de juego
        btnBajarJuego  = crearBoton("Bajar juego");
        btnApoyarJuego = crearBoton("Apoyar ficha");
        btnAgregarPozo = crearBoton("Tirar al pozo");
        btnMenu        = crearBoton("Menú");

        JPanel botones = new JPanel(new GridLayout(4, 1, 0, 8));
        botones.setBackground(C_LATERAL);
        botones.add(btnBajarJuego);
        botones.add(btnApoyarJuego);
        botones.add(btnAgregarPozo);
        botones.add(btnMenu);

        col.add(panelMazo, BorderLayout.NORTH);
        col.add(botones,   BorderLayout.SOUTH);
        return col;
    }

    // ── Tablero central ───────────────────────────────────────────────────────

    private JPanel crearTablero() {
        JPanel tablero = new JPanel(new GridLayout(indicesOtros.length + 1, 1, 0, 4));
        tablero.setBackground(C_MESA);
        tablero.setBorder(new EmptyBorder(8, 8, 8, 8));

        int miEquipo = controlador.getEquipo(miTurno);
        panelesOtros = new JPanel[indicesOtros.length];
        for (int k = 0; k < indicesOtros.length; k++) {
            int indiceOtro = indicesOtros[k];
            JPanel panelOtro = new JPanel();
            panelOtro.setLayout(new BoxLayout(panelOtro, BoxLayout.Y_AXIS));
            panelOtro.setBackground(C_MESA);
            panelesOtros[k] = panelOtro;

            String titulo = "  " + controlador.getNombre(indiceOtro)
                    + (indicesOtros.length > 1
                        ? (controlador.getEquipo(indiceOtro) == miEquipo ? "  ·  Compañero  " : "  ·  Rival  ")
                        : "  ");
            tablero.add(panelConBorde(panelOtro, titulo));
        }

        panelMisJuegos = new JPanel();
        panelMisJuegos.setLayout(new BoxLayout(panelMisJuegos, BoxLayout.Y_AXIS));
        panelMisJuegos.setBackground(C_MESA);
        tablero.add(panelConBorde(panelMisJuegos, "  Mis juegos  "));

        return tablero;
    }

    private JScrollPane panelConBorde(JPanel panel, String titulo) {
        JScrollPane sp = new JScrollPane(panel);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xD4A96A), 1),
                titulo, 0, 0,
                new Font("SansSerif", Font.BOLD, 12), new Color(0xFFE0B2)));
        return sp;
    }

    // ── Columna derecha ───────────────────────────────────────────────────────
    // Contiene: panel del pozo (fichas) + botón "Pozo" para tomar
    // SIN el círculo TÚ

    private JPanel crearColumnaDerecha() {
        JPanel col = new JPanel(new BorderLayout(0, 8));
        col.setBackground(C_LATERAL);
        col.setPreferredSize(new Dimension(130, 0));
        col.setBorder(new EmptyBorder(14, 10, 14, 10));

        // Panel con las fichas del pozo
        panelPozo = new JPanel(new WrapLayout(FlowLayout.CENTER, 4, 4));
        panelPozo.setBackground(C_LATERAL);

        JScrollPane scrollPozo = new JScrollPane(panelPozo);
        scrollPozo.setBorder(BorderFactory.createTitledBorder("Pozo"));

        // Botón "Pozo" para tomar — coherente con su posición visual
        btnTomarPozo = crearBoton("Tomar Pozo");

        col.add(scrollPozo,  BorderLayout.CENTER);
        col.add(btnTomarPozo, BorderLayout.SOUTH);
        return col;
    }

    // ── Franja inferior ───────────────────────────────────────────────────────

    private JPanel crearFranjaInferior() {
        JPanel franja = new JPanel(new BorderLayout(10, 0));
        franja.setBackground(C_MESA_OSCURA);
        franja.setBorder(new EmptyBorder(8, 10, 8, 10));
        franja.setPreferredSize(new Dimension(0, 155));

        panelAtril = new JPanel(new WrapLayout(FlowLayout.LEFT, FICHA_G, FICHA_G));
        panelAtril.setBackground(C_MESA_OSCURA);

        JScrollPane scrollAtril = new JScrollPane(panelAtril,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollAtril.setOpaque(false);
        scrollAtril.getViewport().setBackground(C_MESA_OSCURA);
        scrollAtril.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xD4A96A)),
                "  Tu atril — selecciona fichas en orden y pulsá Bajar juego  ",
                0, 0, new Font("SansSerif", Font.PLAIN, 11), new Color(0xFFE0B2)));

        txtEventos = new JTextArea(5, 26);
        txtEventos.setEditable(false);
        txtEventos.setLineWrap(true);
        txtEventos.setWrapStyleWord(true);
        txtEventos.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtEventos.setBackground(new Color(0x263238));
        txtEventos.setForeground(new Color(0xB2DFDB));

        JScrollPane scrollLog = new JScrollPane(txtEventos);
        scrollLog.setPreferredSize(new Dimension(250, 0));
        scrollLog.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0x546E7A)),
                "  Eventos  ", 0, 0,
                new Font("SansSerif", Font.PLAIN, 11), new Color(0xB0BEC5)));

        franja.add(scrollAtril, BorderLayout.CENTER);
        franja.add(scrollLog,   BorderLayout.EAST);
        return franja;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Acciones
    // ─────────────────────────────────────────────────────────────────────────

    private void conectarAcciones() {
        // Clic en la ficha dibujada del mazo = tomar del mazo
        panelMazo.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (btnTomarMazo.isEnabled()) controlador.agarrarMazo(miTurno);
            }
        });

        btnTomarPozo.addActionListener(e  -> controlador.agarrarPozo(miTurno));
        btnBajarJuego.addActionListener(e -> bajarJuegoSeleccionado());
        btnApoyarJuego.addActionListener(e -> apoyarFichaSeleccionada());
        btnAgregarPozo.addActionListener(e -> agregarFichaAlPozo());
        btnMenu.addActionListener(e       -> mostrarMenuContextual());
    }

    // FIX orden: usa LinkedHashSet en vez de getSelectedIndices()
    private void bajarJuegoSeleccionado() {
        if (indicesSeleccionados.isEmpty()) {
            appendEvento("Seleccioná fichas en orden y pulsá Bajar juego.");
            return;
        }
        int[] indices = new int[indicesSeleccionados.size()];
        int i = 0;
        for (Integer idx : indicesSeleccionados) indices[i++] = idx + 1;
        controlador.bajarJuego(miTurno, indices);
    }

    private void apoyarFichaSeleccionada() {
        if (indicesSeleccionados.size() != 1) {
            appendEvento("Seleccioná exactamente una ficha del atril.");
            return;
        }
        int fichaIdx = indicesSeleccionados.iterator().next();
        Integer juego    = pedirEnteroPositivo("Número de juego en la mesa:");
        if (juego == null) return;
        Integer posicion = pedirEnteroPositivo("Posición donde insertar (1 = inicio):");
        if (posicion == null) return;
        controlador.apoyarJuego(fichaIdx + 1, posicion, miTurno, juego);
    }

    private void agregarFichaAlPozo() {
        if (indicesSeleccionados.size() != 1) {
            appendEvento("Seleccioná exactamente una ficha para tirar al pozo.");
            return;
        }
        controlador.agregarPozo(indicesSeleccionados.iterator().next() + 1, miTurno);
    }

    private void mostrarMenuContextual() {
        JOptionPane.showMessageDialog(this,
                "Turno: " + controlador.getNombre(controlador.getTurnoActual())
                + "\nEstado: " + controlador.getEstadoTurno(),
                "Estado de la partida", JOptionPane.INFORMATION_MESSAGE);
    }

    private Integer pedirEnteroPositivo(String msg) {
        String v = JOptionPane.showInputDialog(this, msg, "Burako", JOptionPane.QUESTION_MESSAGE);
        if (v == null) return null;
        try {
            int n = Integer.parseInt(v.trim());
            if (n < 1) { appendEvento("Ingresá un número positivo."); return null; }
            return n;
        } catch (NumberFormatException ex) {
            appendEvento("Ingresá un número entero.");
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  VistaJuego
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void mostrarMesa() {
        SwingUtilities.invokeLater(() -> {
            for (int k = 0; k < indicesOtros.length; k++) {
                renderizarFilasDeJuegos(panelesOtros[k], controlador.getJuegos(indicesOtros[k]));
            }
            renderizarFilasDeJuegos(panelMisJuegos, controlador.getJuegos(miTurno));
            renderizarPozo(controlador.getPozo());
            renderizarAtril(controlador.getAtril(miTurno));
            actualizarEstadoVisual();
        });
    }

    @Override
    public void mesaje(Eventos evento) {
        SwingUtilities.invokeLater(() -> {
            appendEvento("► " + evento.name());
            if (evento.name().endsWith("_NO_exitoso"))
                appendEvento("  " + controlador.getUltimoMensajeError());
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Renderizado
    // ─────────────────────────────────────────────────────────────────────────

    private void renderizarFilasDeJuegos(JPanel panel, List<JuegoMostrable> juegos) {
        panel.removeAll();
        if (juegos.isEmpty()) {
            JLabel lbl = new JLabel("  — sin juegos —");
            lbl.setForeground(new Color(0xFFE0B2));
            lbl.setFont(new Font("SansSerif", Font.ITALIC, 12));
            panel.add(lbl);
        } else {
            for (int n = 0; n < juegos.size(); n++) {
                JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, FICHA_G, 4));
                fila.setBackground(C_MESA);
                fila.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel num = new JLabel(" " + (n + 1) + " ");
                num.setFont(new Font("SansSerif", Font.BOLD, 12));
                num.setForeground(new Color(0xFFD54F));
                fila.add(num);

                for (FichaMostrable f : juegos.get(n).getFichas())
                    fila.add(crearFicha(f, false));

                panel.add(fila);
                JSeparator sep = new JSeparator();
                sep.setForeground(new Color(0x6D4C41));
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                panel.add(sep);
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    private void renderizarPozo(List<FichaMostrable> fichas) {
        panelPozo.removeAll();
        if (fichas.isEmpty()) {
            JLabel lbl = new JLabel("vacío");
            lbl.setFont(new Font("SansSerif", Font.ITALIC, 11));
            panelPozo.add(lbl);
        } else {
            int ini = Math.max(0, fichas.size() - 8);
            for (int i = ini; i < fichas.size(); i++)
                panelPozo.add(crearFicha(fichas.get(i), false));
        }
        panelPozo.revalidate();
        panelPozo.repaint();
    }

    private void renderizarAtril(List<FichaMostrable> fichas) {
        atrilActual = fichas;
        indicesSeleccionados.clear();
        panelAtril.removeAll();
        for (int i = 0; i < fichas.size(); i++)
            panelAtril.add(crearFichaAtril(fichas.get(i), i));
        panelAtril.revalidate();
        panelAtril.repaint();
    }

    /**
     * Refresca el estado visual de cada ficha del atril.
     *
     * FIX SELECCIÓN: el componente de ficha usa paintComponent() con layout null.
     * Llamar setBackground() no dispara un repaint del paintComponent custom;
     * hay que llamar repaint() explícitamente sobre el panel completo.
     * El número de orden se actualiza en el JLabel "orden" guardado como nombre.
     */
    private void refrescarSeleccionAtril() {
        Component[] comps = panelAtril.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (!(comps[i] instanceof JPanel fichaPanel)) continue;
            boolean sel = indicesSeleccionados.contains(i);

            // Actualizar label de orden
            for (Component h : fichaPanel.getComponents()) {
                if (h instanceof JLabel lbl && "orden".equals(lbl.getName())) {
                    if (sel) {
                        int orden = 1;
                        for (Integer s : indicesSeleccionados) {
                            if (s.equals(i)) break;
                            orden++;
                        }
                        lbl.setText(String.valueOf(orden));
                    } else {
                        lbl.setText("");
                    }
                }
            }
            // repaint() fuerza paintComponent() que lee indicesSeleccionados
            fichaPanel.repaint();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Estado visual de botones y barra
    // ─────────────────────────────────────────────────────────────────────────

    private void actualizarEstadoVisual() {
        int         turno      = controlador.getTurnoActual();
        EstadoTurno estado     = controlador.getEstadoTurno();
        String      nombreTurno = controlador.getNombre(turno);

        String desc;
        if (turno != miTurno) {
            desc = "⏳  Esperando a " + nombreTurno + "…";
        } else if (estado == EstadoTurno.TOMAR) {
            desc = "▶  Tu turno — tomá del mazo o del pozo";
        } else {
            desc = "▶  Tu turno — bajá, apoyá o descartá";
        }

        lblEstado.setText("  " + controlador.getNombre(miTurno)
                + "   |   Turno: " + nombreTurno
                + "   |   " + desc + "  ");

        boolean puedeTomar = controlador.puedeTomar(miTurno);
        boolean puedeJugar = controlador.puedeJugar(miTurno);

        btnTomarMazo.setEnabled(puedeTomar);
        btnTomarPozo.setEnabled(puedeTomar);
        btnBajarJuego.setEnabled(puedeJugar);
        btnApoyarJuego.setEnabled(puedeJugar);
        btnAgregarPozo.setEnabled(puedeJugar);

        // Borde del mazo: rojo cuando se puede tomar, gris cuando no
        panelMazo.setBorder(puedeTomar
                ? BorderFactory.createLineBorder(C_FICHA_R, 2)
                : BorderFactory.createLineBorder(Color.GRAY, 1));
        panelMazo.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Componentes de ficha
    // ─────────────────────────────────────────────────────────────────────────

    /** Ficha estática (mesa / pozo). */
    private JPanel crearFicha(FichaMostrable f, boolean seleccionada) {
        Color col = colorDeFicha(f);
        JPanel comp = new JPanel(new BorderLayout(0, 1)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 55));
                g2.fillRoundRect(3, 3, FICHA_W, FICHA_H, FICHA_R, FICHA_R);
                g2.setColor(seleccionada ? C_SELEC_FONDO : C_FICHA_FONDO);
                g2.fillRoundRect(0, 0, FICHA_W, FICHA_H, FICHA_R, FICHA_R);
                g2.setColor(col);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, FICHA_W - 2, FICHA_H - 2, FICHA_R, FICHA_R);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(FICHA_W + 3, FICHA_H + 3); }
        };
        comp.setOpaque(false);

        JLabel num = new JLabel(etiquetaNumero(f), SwingConstants.CENTER);
        num.setFont(new Font("SansSerif", Font.BOLD, 14));
        num.setForeground(col);
        comp.add(num, BorderLayout.CENTER);

        JLabel abr = new JLabel(etiquetaColor(f), SwingConstants.CENTER);
        abr.setFont(new Font("SansSerif", Font.PLAIN, 9));
        abr.setForeground(col.darker());
        comp.add(abr, BorderLayout.SOUTH);

        comp.setToolTipText(f.getColor().name() + " " + f.getNum().name());
        return comp;
    }

    /**
     * Ficha clickable del atril.
     *
     * SELECCIÓN VISUAL:
     * - paintComponent() dibuja el fondo cyan (C_SELEC_FONDO) y borde rojo
     *   grueso cuando indicesSeleccionados contiene este idx.
     * - Un JLabel "orden" en la esquina sup-der muestra el número de orden
     *   de selección (1, 2, 3…) para que el jugador sepa en qué posición
     *   del juego irá cada ficha.
     * - refrescarSeleccionAtril() llama repaint() sobre el panel, que
     *   dispara paintComponent() y refleja el nuevo estado.
     */
    private JPanel crearFichaAtril(FichaMostrable f, int idx) {
        Color col = colorDeFicha(f);

        JPanel comp = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = indicesSeleccionados.contains(idx);

                // Sombra
                g2.setColor(new Color(0, 0, 0, 80));
                g2.fillRoundRect(3, 3, FICHA_W, FICHA_H, FICHA_R, FICHA_R);

                // Fondo: cyan claro si seleccionada, crema si no
                g2.setColor(sel ? C_SELEC_FONDO : C_FICHA_FONDO);
                g2.fillRoundRect(0, 0, FICHA_W, FICHA_H, FICHA_R, FICHA_R);

                // Borde: rojo grueso si seleccionada, color de ficha si no
                g2.setColor(sel ? C_SELEC_BORDE : col);
                g2.setStroke(new BasicStroke(sel ? 3.5f : 2f));
                g2.drawRoundRect(1, 1, FICHA_W - 2, FICHA_H - 2, FICHA_R, FICHA_R);

                // Segunda línea interior cuando seleccionada (efecto "recuadro")
                if (sel) {
                    g2.setColor(new Color(0xC62828, false));
                    g2.setColor(new Color(0xEF9A9A));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(5, 5, FICHA_W - 10, FICHA_H - 10, FICHA_R - 2, FICHA_R - 2);
                }
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(FICHA_W + 4, FICHA_H + 4); }
        };
        comp.setOpaque(false);
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Número de la ficha
        JLabel lblNum = new JLabel(etiquetaNumero(f), SwingConstants.CENTER);
        lblNum.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblNum.setForeground(col);
        lblNum.setBounds(0, 9, FICHA_W, 28);
        comp.add(lblNum);

        // Abreviatura del color
        JLabel lblAbr = new JLabel(etiquetaColor(f), SwingConstants.CENTER);
        lblAbr.setFont(new Font("SansSerif", Font.PLAIN, 9));
        lblAbr.setForeground(col.darker());
        lblAbr.setBounds(0, FICHA_H - 17, FICHA_W, 13);
        comp.add(lblAbr);

        // Número de orden de selección (esquina sup-der)
        JLabel lblOrden = new JLabel("", SwingConstants.CENTER);
        lblOrden.setName("orden");
        lblOrden.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblOrden.setForeground(C_SELEC_BORDE);
        lblOrden.setBounds(FICHA_W - 17, 2, 17, 15);
        comp.add(lblOrden);

        comp.setToolTipText(f.getColor().name() + " " + f.getNum().name());

        comp.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (indicesSeleccionados.contains(idx))
                    indicesSeleccionados.remove(idx);
                else
                    indicesSeleccionados.add(idx);
                refrescarSeleccionAtril();
            }
        });

        return comp;
    }

    /** Dibuja la ficha-dorso del mazo (azul oscuro con texto "MAZO"). */
    private void dibujarFichaDorso(Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Sombra
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillRoundRect(x + 3, y + 3, FICHA_W, FICHA_H, FICHA_R, FICHA_R);
        // Fondo azul
        g2.setColor(new Color(0x1A237E));
        g2.fillRoundRect(x, y, FICHA_W, FICHA_H, FICHA_R, FICHA_R);
        // Borde blanco
        g2.setColor(new Color(0xE8EAF6));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x + 1, y + 1, FICHA_W - 2, FICHA_H - 2, FICHA_R, FICHA_R);
        // Texto
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        String txt = "MAZO";
        g2.drawString(txt,
                x + (FICHA_W - fm.stringWidth(txt)) / 2,
                y + FICHA_H / 2 + fm.getAscent() / 2 - 2);
        g2.dispose();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Color colorDeFicha(FichaMostrable f) {
        return switch (f.getColor().name()) {
            case "Rojo"     -> C_FICHA_R;
            case "Azul"     -> C_FICHA_A;
            case "Amarillo" -> C_FICHA_AM;
            case "Negro"    -> C_FICHA_N;
            default         -> Color.GRAY;
        };
    }

    private String etiquetaNumero(FichaMostrable f) {
        String n = f.getNum().name();
        return n.equals("Comodin") ? "★" : n.replaceAll("[^0-9]", "");
    }

    private String etiquetaColor(FichaMostrable f) {
        return switch (f.getColor().name()) {
            case "Rojo"     -> "R";
            case "Azul"     -> "A";
            case "Amarillo" -> "AM";
            case "Negro"    -> "N";
            default         -> "?";
        };
    }

    private JButton crearBoton(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(0x37474F));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x546E7A), 1),
                new EmptyBorder(6, 10, 6, 10)));
        return btn;
    }

    private void appendEvento(String texto) {
        txtEventos.append(texto + "\n");
        txtEventos.setCaretPosition(txtEventos.getDocument().getLength());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  WrapLayout
    // ─────────────────────────────────────────────────────────────────────────

    private static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override public Dimension preferredLayoutSize(Container t) { return layoutSize(t, true); }
        @Override public Dimension minimumLayoutSize(Container t) {
            Dimension d = layoutSize(t, false); d.width -= getHgap() + 1; return d;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int tw = target.getSize().width;
                if (tw == 0) tw = Integer.MAX_VALUE;
                Insets ins = target.getInsets();
                int maxW = tw - (ins.left + ins.right + getHgap() * 2);
                Dimension dim = new Dimension(0, 0);
                int rowW = 0, rowH = 0;
                for (Component c : target.getComponents()) {
                    if (!c.isVisible()) continue;
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (rowW + d.width > maxW) {
                        dim.width = Math.max(dim.width, rowW);
                        dim.height += rowH + getVgap();
                        rowW = 0; rowH = 0;
                    }
                    if (rowW != 0) rowW += getHgap();
                    rowW += d.width;
                    rowH = Math.max(rowH, d.height);
                }
                dim.width  = Math.max(dim.width, rowW);
                dim.height += rowH + ins.top + ins.bottom + getVgap() * 2;
                return dim;
            }
        }
    }
}