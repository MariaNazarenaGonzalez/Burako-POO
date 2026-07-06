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
    private static final Color C_FICHA_AM    = new Color(0xD4860A);
    private static final Color C_FICHA_N     = new Color(0x212121);
    private static final Color C_FICHA_FONDO = new Color(0xFFFDE7);
    private static final Color C_SEL_FONDO   = new Color(0xFFF176); // amarillo vivo
    private static final Color C_SEL_BORDE   = new Color(0xE53935); // rojo intenso

    // ── Dimensiones de ficha ──────────────────────────────────────────────────
    private static final int FW = 52;   // ancho ficha
    private static final int FH = 68;   // alto  ficha
    private static final int FR = 10;   // radio esquinas
    private static final int FG = 7;    // gap entre fichas

    // ── Dependencias ──────────────────────────────────────────────────────────
    private final Controlador  controlador;
    private final int          miTurno;

    /**
     * Preserva el ORDEN DE CLIC del usuario.
     * LinkedHashSet garantiza: sin duplicados + orden de inserción.
     * Esto es esencial para que una escalera seleccionada en orden
     * no-consecutivo llegue al modelo en el orden correcto.
     */
    private final Set<Integer> seleccion = new LinkedHashSet<>();

    // ── Componentes ───────────────────────────────────────────────────────────
    private JLabel    lblEstado;
    private JPanel    panelJuegosRival;
    private JPanel    panelMisJuegos;
    private JPanel    panelMazo;
    private JPanel    panelPozo;
    private JPanel    panelAtril;
    private JButton   btnTomarMazo;   // invisible, controla enabled
    private JButton   btnTomarPozo;
    private JButton   btnBajarJuego;
    private JButton   btnApoyarJuego;
    private JButton   btnAgregarPozo;
    private JTextArea txtEventos;

    // ─────────────────────────────────────────────────────────────────────────
    public VistaGrafica(Controlador controlador, int turno) {
        this.controlador = controlador;
        this.miTurno     = turno;
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
    //  Construcción UI
    // ─────────────────────────────────────────────────────────────────────────

    private void crearUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_MESA);
        setContentPane(root);

        lblEstado = new JLabel("Estado");
        lblEstado.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblEstado.setForeground(Color.WHITE);
        lblEstado.setOpaque(true);
        lblEstado.setBackground(new Color(0x37474F));
        lblEstado.setBorder(new EmptyBorder(7, 14, 7, 14));
        root.add(lblEstado, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(C_MESA);
        root.add(centro, BorderLayout.CENTER);
        centro.add(crearColumnaIzq(), BorderLayout.WEST);
        centro.add(crearTablero(),    BorderLayout.CENTER);
        centro.add(crearColumnaDer(), BorderLayout.EAST);

        root.add(crearFranjaInferior(), BorderLayout.SOUTH);
    }

    private JPanel crearColumnaIzq() {
        JPanel col = new JPanel(new BorderLayout(0, 12));
        col.setBackground(C_LATERAL);
        col.setPreferredSize(new Dimension(130, 0));
        col.setBorder(new EmptyBorder(14, 10, 14, 10));

        // Ficha-mazo clickable (sin botón duplicado)
        panelMazo = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujarDorso(g, (getWidth() - FW) / 2, 6);
            }
        };
        panelMazo.setBackground(C_LATERAL);
        panelMazo.setPreferredSize(new Dimension(110, 90));
        panelMazo.setToolTipText("Clic para tomar del mazo");
        panelMazo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnTomarMazo = new JButton(); // invisible — solo para controlar enabled
        btnTomarMazo.setVisible(false);

        btnBajarJuego  = crearBoton("Bajar juego");
        btnApoyarJuego = crearBoton("Apoyar ficha");
        btnAgregarPozo = crearBoton("Tirar al pozo");

        JPanel botones = new JPanel(new GridLayout(4, 1, 0, 8));
        botones.setBackground(C_LATERAL);
        botones.add(btnBajarJuego);
        botones.add(btnApoyarJuego);
        botones.add(btnAgregarPozo);

        col.add(panelMazo, BorderLayout.NORTH);
        col.add(botones,   BorderLayout.SOUTH);
        return col;
    }

    private JPanel crearTablero() {
        JPanel tablero = new JPanel(new GridLayout(2, 1, 0, 4));
        tablero.setBackground(C_MESA);
        tablero.setBorder(new EmptyBorder(8, 8, 8, 8));

        panelJuegosRival = panelJuego();
        panelMisJuegos   = panelJuego();

        tablero.add(scrollConTitulo(panelJuegosRival, "  Rival  "));
        tablero.add(scrollConTitulo(panelMisJuegos,   "  Mis juegos  "));
        return tablero;
    }

    private JPanel panelJuego() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_MESA);
        return p;
    }

    private JScrollPane scrollConTitulo(JPanel panel, String titulo) {
        JScrollPane sp = new JScrollPane(panel);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xD4A96A), 1),
                titulo, 0, 0,
                new Font("SansSerif", Font.BOLD, 12), new Color(0xFFE0B2)));
        return sp;
    }

    private JPanel crearColumnaDer() {
        JPanel col = new JPanel(new BorderLayout(0, 8));
        col.setBackground(C_LATERAL);
        col.setPreferredSize(new Dimension(130, 0));
        col.setBorder(new EmptyBorder(14, 10, 14, 10));

        panelPozo = new JPanel(new WrapLayout(FlowLayout.CENTER, 4, 4));
        panelPozo.setBackground(C_LATERAL);

        JScrollPane sp = new JScrollPane(panelPozo);
        sp.setBorder(BorderFactory.createTitledBorder("Pozo"));

        btnTomarPozo = crearBoton("Tomar Pozo");

        col.add(sp,          BorderLayout.CENTER);
        col.add(btnTomarPozo, BorderLayout.SOUTH);
        return col;
    }

    private JPanel crearFranjaInferior() {
        JPanel franja = new JPanel(new BorderLayout(10, 0));
        franja.setBackground(C_MESA_OSCURA);
        franja.setBorder(new EmptyBorder(8, 10, 8, 10));
        franja.setPreferredSize(new Dimension(0, 160));

        panelAtril = new JPanel(new WrapLayout(FlowLayout.LEFT, FG, FG));
        panelAtril.setBackground(C_MESA_OSCURA);

        JScrollPane scrollAtril = new JScrollPane(panelAtril,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollAtril.setOpaque(false);
        scrollAtril.getViewport().setBackground(C_MESA_OSCURA);
        scrollAtril.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xD4A96A)),
                "  Tu atril — hacé clic en las fichas en el orden que querés bajarlas  ",
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
        panelMazo.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (btnTomarMazo.isEnabled()) controlador.agarrarMazo(miTurno);
            }
        });
        btnTomarPozo.addActionListener(e  -> controlador.agarrarPozo(miTurno));
        btnBajarJuego.addActionListener(e -> bajarJuegoSeleccionado());
        btnApoyarJuego.addActionListener(e -> apoyarFichaSeleccionada());
        btnAgregarPozo.addActionListener(e -> agregarFichaAlPozo());
    }

    private void bajarJuegoSeleccionado() {
        if (seleccion.isEmpty()) {
            appendEvento("Seleccioná fichas en orden y pulsá Bajar juego."); return;
        }
        int[] idx = new int[seleccion.size()];
        int i = 0;
        for (Integer s : seleccion) idx[i++] = s + 1;
        controlador.bajarJuego(miTurno, idx);
    }

    private void apoyarFichaSeleccionada() {
        if (seleccion.size() != 1) {
            appendEvento("Seleccioná exactamente una ficha."); return;
        }
        Integer juego = pedirInt("Número de juego en la mesa:"); if (juego == null) return;
        Integer pos   = pedirInt("Posición donde insertar:");    if (pos   == null) return;
        controlador.apoyarJuego(seleccion.iterator().next() + 1, pos, miTurno, juego);
    }

    private void agregarFichaAlPozo() {
        if (seleccion.size() != 1) {
            appendEvento("Seleccioná exactamente una ficha para tirar al pozo."); return;
        }
        controlador.agregarPozo(seleccion.iterator().next() + 1, miTurno);
    }

    private Integer pedirInt(String msg) {
        String v = JOptionPane.showInputDialog(this, msg, "Burako", JOptionPane.QUESTION_MESSAGE);
        if (v == null) return null;
        try { int n = Integer.parseInt(v.trim()); return n >= 1 ? n : null; }
        catch (NumberFormatException ex) { appendEvento("Ingresá un número."); return null; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  VistaJuego
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void mostrarMesa() {
        SwingUtilities.invokeLater(() -> {
            renderizarJuegos(panelJuegosRival, controlador.getJuegos((miTurno + 1) % 2));
            renderizarJuegos(panelMisJuegos,   controlador.getJuegos(miTurno));
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

    private void renderizarJuegos(JPanel panel, List<JuegoMostrable> juegos) {
        panel.removeAll();
        if (juegos.isEmpty()) {
            JLabel lbl = new JLabel("  — sin juegos —");
            lbl.setForeground(new Color(0xFFE0B2));
            lbl.setFont(new Font("SansSerif", Font.ITALIC, 12));
            panel.add(lbl);
        } else {
            for (int n = 0; n < juegos.size(); n++) {
                JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, FG, 4));
                fila.setBackground(C_MESA);
                fila.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel num = new JLabel(" " + (n + 1) + " ");
                num.setFont(new Font("SansSerif", Font.BOLD, 12));
                num.setForeground(new Color(0xFFD54F));
                fila.add(num);
                for (FichaMostrable f : juegos.get(n).getFichas())
                    fila.add(new FichaPanel(f, -1));  // -1 = no clickable
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
                panelPozo.add(new FichaPanel(fichas.get(i), -1));
        }
        panelPozo.revalidate();
        panelPozo.repaint();
    }

    private void renderizarAtril(List<FichaMostrable> fichas) {
        seleccion.clear();
        panelAtril.removeAll();
        for (int i = 0; i < fichas.size(); i++)
            panelAtril.add(new FichaPanel(fichas.get(i), i));
        panelAtril.revalidate();
        panelAtril.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  FichaPanel — componente de ficha totalmente autodibujado
    //
    //  La clave del fix de selección: NO hay componentes hijos ni layout null.
    //  Todo (número, color, borde, fondo, número de orden) se dibuja en un
    //  único paintComponent(). Cuando cambia seleccion, basta llamar
    //  repaint() sobre este panel y Swing lo redibuja completo sin ambigüedad.
    // ─────────────────────────────────────────────────────────────────────────

    private class FichaPanel extends JPanel {
        private final FichaMostrable ficha;
        private final int            idx;   // -1 si no es del atril

        FichaPanel(FichaMostrable ficha, int idx) {
            this.ficha = ficha;
            this.idx   = idx;
            setOpaque(false);
            if (idx >= 0) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setToolTipText(ficha.getColor().name() + " " + ficha.getNum().name()
                        + " — clic para seleccionar");
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        if (seleccion.contains(idx)) seleccion.remove(idx);
                        else                          seleccion.add(idx);
                        // Repintar TODOS los paneles del atril para actualizar
                        // números de orden (si desmarco el 1º, el 2º pasa a ser 1º)
                        for (Component c : panelAtril.getComponents()) c.repaint();
                    }
                });
            }
        }

        @Override public Dimension getPreferredSize() {
            return new Dimension(FW + 4, FH + 4);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            boolean sel = idx >= 0 && seleccion.contains(idx);
            Color   col = colorDeFicha(ficha);

            // ── Sombra ────────────────────────────────────────────────────────
            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillRoundRect(3, 3, FW, FH, FR, FR);

            // ── Fondo ─────────────────────────────────────────────────────────
            g2.setColor(sel ? C_SEL_FONDO : C_FICHA_FONDO);
            g2.fillRoundRect(0, 0, FW, FH, FR, FR);

            // ── Borde principal ───────────────────────────────────────────────
            if (sel) {
                // Borde rojo doble: exterior grueso + interior fino
                g2.setColor(C_SEL_BORDE);
                g2.setStroke(new BasicStroke(3.5f));
                g2.drawRoundRect(2, 2, FW - 4, FH - 4, FR, FR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(6, 6, FW - 12, FH - 12, FR - 3, FR - 3);
            } else {
                g2.setColor(col);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, FW - 2, FH - 2, FR, FR);
            }

            // ── Número de la ficha (centro) ───────────────────────────────────
            String numStr = etiquetaNumero(ficha);
            g2.setFont(new Font("SansSerif", Font.BOLD, 17));
            FontMetrics fm = g2.getFontMetrics();
            int nx = (FW - fm.stringWidth(numStr)) / 2;
            int ny = FH / 2 + fm.getAscent() / 2 - 4;
            g2.setColor(sel ? col.darker() : col);
            g2.drawString(numStr, nx, ny);

            // ── Abreviatura del color (parte inferior) ────────────────────────
            String abrStr = etiquetaColor(ficha);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            fm = g2.getFontMetrics();
            g2.setColor(sel ? col.darker() : col.darker());
            g2.drawString(abrStr, (FW - fm.stringWidth(abrStr)) / 2, FH - 5);

            // ── Número de orden de selección (esquina sup-izq) ───────────────
            // Visible, grande y con fondo blanco para máximo contraste
            if (sel) {
                int orden = ordenDeSeleccion(idx);
                String ordenStr = String.valueOf(orden);

                // Círculo de fondo blanco
                g2.setColor(Color.WHITE);
                g2.fillOval(2, 2, 18, 18);
                g2.setColor(C_SEL_BORDE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(2, 2, 18, 18);

                // Número dentro del círculo
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                fm = g2.getFontMetrics();
                g2.setColor(C_SEL_BORDE);
                g2.drawString(ordenStr,
                        2 + (18 - fm.stringWidth(ordenStr)) / 2,
                        2 + fm.getAscent() + (18 - fm.getHeight()) / 2);
            }

            g2.dispose();
        }

        /** Posición 1-based de este índice en el orden de selección actual. */
        private int ordenDeSeleccion(int targetIdx) {
            int orden = 1;
            for (Integer s : seleccion) {
                if (s.equals(targetIdx)) return orden;
                orden++;
            }
            return orden;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Estado visual
    // ─────────────────────────────────────────────────────────────────────────

    private void actualizarEstadoVisual() {
        int         turno      = controlador.getTurnoActual();
        EstadoTurno estado     = controlador.getEstadoTurno();
        String      nombreTurno = controlador.getNombre(turno);

        String desc = turno != miTurno
                ? "⏳  Esperando a " + nombreTurno + "…"
                : estado == EstadoTurno.TOMAR
                        ? "▶  Tu turno — tomá del mazo o del pozo"
                        : "▶  Tu turno — bajá, apoyá o descartá";

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

        panelMazo.setBorder(puedeTomar
                ? BorderFactory.createLineBorder(C_FICHA_R, 2)
                : BorderFactory.createLineBorder(Color.GRAY, 1));
        panelMazo.repaint();
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

    private void dibujarDorso(Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillRoundRect(x + 3, y + 3, FW, FH, FR, FR);
        g2.setColor(new Color(0x1A237E));
        g2.fillRoundRect(x, y, FW, FH, FR, FR);
        g2.setColor(new Color(0xE8EAF6));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x + 1, y + 1, FW - 2, FH - 2, FR, FR);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        String txt = "MAZO";
        g2.drawString(txt, x + (FW - fm.stringWidth(txt)) / 2,
                y + FH / 2 + fm.getAscent() / 2 - 2);
        g2.dispose();
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

        @Override public Dimension preferredLayoutSize(Container t) { return calc(t, true); }
        @Override public Dimension minimumLayoutSize(Container t)   { return calc(t, false); }

        private Dimension calc(Container t, boolean pref) {
            synchronized (t.getTreeLock()) {
                int tw = t.getSize().width; if (tw == 0) tw = Integer.MAX_VALUE;
                Insets ins = t.getInsets();
                int maxW = tw - ins.left - ins.right - getHgap() * 2;
                Dimension dim = new Dimension(0, 0);
                int rw = 0, rh = 0;
                for (Component c : t.getComponents()) {
                    if (!c.isVisible()) continue;
                    Dimension d = pref ? c.getPreferredSize() : c.getMinimumSize();
                    if (rw + d.width > maxW) {
                        dim.width = Math.max(dim.width, rw);
                        dim.height += rh + getVgap();
                        rw = 0; rh = 0;
                    }
                    if (rw != 0) rw += getHgap();
                    rw += d.width; rh = Math.max(rh, d.height);
                }
                dim.width  = Math.max(dim.width, rw);
                dim.height += rh + ins.top + ins.bottom + getVgap() * 2;
                return dim;
            }
        }
    }
}
