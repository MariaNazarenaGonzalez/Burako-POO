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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class VistaGrafica extends JFrame implements VistaJuego {
    private final Controlador controlador;
    private final int miTurno;
    private final Set<Integer> indicesSeleccionados;
    private DefaultListModel<String> modeloAtril;

    private JPanel panelPrincipal;
    private JLabel lblEstado;
    private JPanel panelJuegosRival;
    private JPanel panelMisJuegos;
    private JPanel panelPozo;
    private JList<String> listaAtril;
    private JLabel lblAyudaAtril;
    private JButton btnTomarMazo;
    private JButton btnTomarPozo;
    private JButton btnBajarJuego;
    private JButton btnApoyarJuego;
    private JButton btnAgregarPozo;
    private JTextArea txtEventos;

    public VistaGrafica(Controlador controlador, int turno) {
        this.controlador = controlador;
        this.miTurno = turno;
        this.indicesSeleccionados = new LinkedHashSet<>();

        setTitle("Burako - " + getNombre(miTurno));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationByPlatform(true);
        setContentPane(panelPrincipal);

        inicializarForma();
        conectarAcciones();
        mostrarMesa();
        appendEvento("Vista grafica lista para " + getNombre(miTurno) + ".");
        actualizarEstadoVisual();
    }

    private void inicializarForma() {
        lblEstado.setFont(new Font("SansSerif", Font.BOLD, 18));

        configurarPanelSeccion(panelJuegosRival, "Juegos del rival");
        configurarPanelSeccion(panelMisJuegos, "Tus juegos");
        configurarPanelSeccion(panelPozo, "Pozo");

        modeloAtril = new DefaultListModel<>();
        listaAtril.setModel(modeloAtril);
        listaAtril.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaAtril.setVisibleRowCount(18);
        listaAtril.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String prefijo = indicesSeleccionados.contains(index) ? "[x] " : "[ ] ";
                label.setText(prefijo + value);
                return label;
            }
        });
        listaAtril.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int indice = listaAtril.locationToIndex(e.getPoint());
                if (indice >= 0) {
                    alternarSeleccionAtril(indice);
                    e.consume();
                }
            }
        });

        lblAyudaAtril.setText("Clic para marcar o desmarcar fichas del atril.");
        txtEventos.setEditable(false);
        txtEventos.setLineWrap(true);
        txtEventos.setWrapStyleWord(true);
    }

    private void configurarPanelSeccion(JPanel panel, String titulo) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
    }

    private void alternarSeleccionAtril(int indice) {
        if (indicesSeleccionados.contains(indice)) {
            indicesSeleccionados.remove(indice);
        } else {
            indicesSeleccionados.add(indice);
        }
        sincronizarSeleccionAtril();
        listaAtril.repaint();
    }

    private void conectarAcciones() {
        btnTomarMazo.addActionListener(e -> tomarDelMazo());
        btnTomarPozo.addActionListener(e -> tomarDelPozo());
        btnBajarJuego.addActionListener(e -> bajarJuegoSeleccionado());
        btnApoyarJuego.addActionListener(e -> apoyarFichaSeleccionada());
        btnAgregarPozo.addActionListener(e -> agregarFichaAlPozo());
    }

    private void tomarDelMazo() {
        controlador.agarrarMazo(miTurno);
    }

    private void tomarDelPozo() {
        controlador.agarrarPozo(miTurno);
    }

    private void bajarJuegoSeleccionado() {
        int[] seleccion = listaAtril.getSelectedIndices();
        if (seleccion.length < 3) {
            appendEvento("Selecciona al menos 3 fichas para bajar un juego.");
            return;
        }
        int[] indices = new int[seleccion.length];
        for (int i = 0; i < seleccion.length; i++) {
            indices[i] = seleccion[i] + 1;
        }
        controlador.bajarJuego(miTurno, indices);
        mostrarMesa();
    }

    private void apoyarFichaSeleccionada() {
        int fichaSeleccionada = listaAtril.getSelectedIndex();
        if (fichaSeleccionada < 0 || listaAtril.getSelectedIndices().length != 1) {
            appendEvento("Selecciona una ficha del atril.");
            return;
        }
        int cantidadJuegos = controlador.cantJuegos(miTurno);
        if (cantidadJuegos == 0) {
            appendEvento("No tienes juegos para apoyar.");
            return;
        }

        Integer juego = pedirNumero("Numero de juego (1-" + cantidadJuegos + "):", 1, cantidadJuegos);
        if (juego == null) {
            return;
        }
        int cantidadFichas = controlador.getJuegos(miTurno).get(juego - 1).getJuego().size();
        Integer posicion = pedirNumero("Posicion donde agregar la ficha (1-" + (cantidadFichas + 1) + "):", 1, cantidadFichas + 1);
        if (posicion == null) {
            return;
        }

        controlador.apoyarJuego(fichaSeleccionada + 1, posicion, miTurno, juego);
        mostrarMesa();
    }

    private void agregarFichaAlPozo() {
        int fichaSeleccionada = listaAtril.getSelectedIndex();
        if (fichaSeleccionada < 0 || listaAtril.getSelectedIndices().length != 1) {
            appendEvento("Selecciona una sola ficha para tirar al pozo.");
            return;
        }
        controlador.agregarPozo(fichaSeleccionada + 1, miTurno);
        mostrarMesa();
    }

    private Integer pedirNumero(String mensaje, int minimo, int maximo) {
        String valor = JOptionPane.showInputDialog(this, mensaje, "Burako", JOptionPane.QUESTION_MESSAGE);
        if (valor == null) {
            return null;
        }
        try {
            int numero = Integer.parseInt(valor);
            if (numero < minimo || numero > maximo) {
                appendEvento("El valor debe estar entre " + minimo + " y " + maximo + ".");
                return null;
            }
            return numero;
        } catch (NumberFormatException e) {
            appendEvento("Debes ingresar un numero.");
            return null;
        }
    }

    @Override
    public void mostrarMesa() {
        renderizarJuegos(panelJuegosRival, controlador.getJuegos((miTurno + 1) % 2), "Sin juegos");
        renderizarJuegos(panelMisJuegos, controlador.getJuegos(miTurno), "Sin juegos");
        renderizarFichas(panelPozo, controlador.getPozo(), "Pozo vacio");
        renderizarAtril(controlador.getAtril(miTurno));
        actualizarEstadoVisual();
    }

    @Override
    public void mesaje(Eventos eventos) {
        appendEvento(eventos.name());
        mostrarMesa();
    }

    @Override
    public void mesaje(Exception e) {
        appendEvento(e.getMessage());
    }

    private void renderizarJuegos(JPanel panel, ArrayList<JuegoMostrable> juegos, String mensajeVacio) {
        panel.removeAll();
        if (juegos.isEmpty()) {
            panel.add(crearEtiqueta(mensajeVacio));
        } else {
            int numeroJuego = 1;
            for (JuegoMostrable juego : juegos) {
                panel.add(crearEtiqueta("Juego " + numeroJuego + ": " + formatearJuego(juego)));
                numeroJuego++;
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    private void renderizarFichas(JPanel panel, ArrayList<FichaMostrable> fichas, String mensajeVacio) {
        panel.removeAll();
        if (fichas.isEmpty()) {
            panel.add(crearEtiqueta(mensajeVacio));
        } else {
            StringBuilder texto = new StringBuilder();
            for (FichaMostrable ficha : fichas) {
                if (!texto.isEmpty()) {
                    texto.append(" ");
                }
                texto.append(formatearFicha(ficha));
            }
            panel.add(crearEtiqueta(texto.toString()));
        }
        panel.revalidate();
        panel.repaint();
    }

    private void renderizarAtril(ArrayList<FichaMostrable> atril) {
        indicesSeleccionados.clear();
        listaAtril.clearSelection();
        modeloAtril.clear();
        for (int i = 0; i < atril.size(); i++) {
            modeloAtril.addElement((i + 1) + " - " + formatearFicha(atril.get(i)));
        }
    }

    private void sincronizarSeleccionAtril() {
        listaAtril.clearSelection();
        for (Integer indice : indicesSeleccionados) {
            listaAtril.addSelectionInterval(indice, indice);
        }
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel label = new JLabel("<html><body style='width:220px'>" + texto + "</body></html>");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private String formatearJuego(JuegoMostrable juego) {
        StringBuilder texto = new StringBuilder();
        for (FichaMostrable ficha : juego.getJuego()) {
            if (!texto.isEmpty()) {
                texto.append(" ");
            }
            texto.append(formatearFicha(ficha));
        }
        return texto.toString();
    }

    private String formatearFicha(FichaMostrable ficha) {
        return "[" + ficha.getColor().name() + "_" + ficha.getNum().name() + "]";
    }

    private void appendEvento(String texto) {
        txtEventos.append(texto + "\n");
        txtEventos.setCaretPosition(txtEventos.getDocument().getLength());
    }

    private void actualizarEstadoVisual() {
        int turnoActual = controlador.getTurnoActual();
        EstadoTurno estadoTurno = controlador.getEstadoTurno();
        String nombreTurno = getNombre(turnoActual);
        String descripcionEstado;
        if (!esMiTurno()) {
            descripcionEstado = "Esperando a " + nombreTurno;
        } else if (estadoTurno == EstadoTurno.TOMAR) {
            descripcionEstado = "Tu turno: toma del mazo o del pozo";
        } else {
            descripcionEstado = "Tu turno: baja, apoya o tira una ficha al pozo";
        }
        lblEstado.setText("Jugador: " + getNombre(miTurno) + " | Turno actual: " + nombreTurno + " | " + descripcionEstado);

        boolean puedeTomar = controlador.puedeTomar(miTurno);
        boolean puedeJugar = controlador.puedeJugar(miTurno);
        btnTomarMazo.setEnabled(puedeTomar);
        btnTomarPozo.setEnabled(puedeTomar);
        btnBajarJuego.setEnabled(puedeJugar);
        btnApoyarJuego.setEnabled(puedeJugar);
        btnAgregarPozo.setEnabled(puedeJugar);
    }

    private boolean esMiTurno() {
        return controlador.getTurnoActual() == miTurno;
    }

    private String getNombre(int turno) {
        return controlador.getNombre(turno);
    }
}
