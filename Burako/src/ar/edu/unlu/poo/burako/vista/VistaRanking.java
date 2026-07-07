package ar.edu.unlu.poo.burako.vista;

import ar.edu.unlu.poo.burako.persistencia.EntradaRanking;
import ar.edu.unlu.poo.burako.persistencia.IServicioRanking;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**

* Ventana que muestra el ranking de jugadores en forma de tabla.
*
* Recibe una colección de resultados ya preparada y la presenta en una
* tabla de solo lectura, permitiendo consultar el historial de puntajes
* y estadísticas de cada jugador.
*/

public class VistaRanking extends JDialog {

    private static final String[] COLUMNAS = {
            "Posición", "Usuario", "Puntos", "Victorias", "Derrotas", "Partidas jugadas"
    };

    public VistaRanking(JFrame padre, List<EntradaRanking> ranking) {
        super(padre, "Ranking", true);
        setLayout(new BorderLayout());

        if (ranking.isEmpty()) {
            add(new JLabel("Todavía no hay datos de ranking.", SwingConstants.CENTER), BorderLayout.CENTER);
        } else {
            add(new JScrollPane(construirTabla(ranking)), BorderLayout.CENTER);
        }

        JButton botonCerrar = new JButton("Cerrar");
        botonCerrar.addActionListener(e -> dispose());
        JPanel panelInferior = new JPanel();
        panelInferior.add(botonCerrar);
        add(panelInferior, BorderLayout.SOUTH);

        setSize(520, 380);
        setLocationRelativeTo(padre);
    }

    private JTable construirTabla(List<EntradaRanking> ranking) {
        Object[][] datos = new Object[ranking.size()][COLUMNAS.length];
        for (int i = 0; i < ranking.size(); i++) {
            EntradaRanking entrada = ranking.get(i);
            datos[i][0] = i + 1;
            datos[i][1] = entrada.getNombre();
            datos[i][2] = entrada.getPuntajeAcumulado();
            datos[i][3] = entrada.getVictorias();
            datos[i][4] = entrada.getDerrotas();
            datos[i][5] = entrada.getPartidasJugadas();
        }

        DefaultTableModel modelo = new DefaultTableModel(datos, COLUMNAS) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        JTable tabla = new JTable(modelo);
        tabla.setFillsViewportHeight(true);
        return tabla;
    }

    public static void mostrarConectandoAServidor(JFrame padre) {
        String ipServidor = JOptionPane.showInputDialog(padre, "IP del servidor:",
                "Ranking - Conexión", JOptionPane.QUESTION_MESSAGE);
        if (ipServidor == null || ipServidor.isBlank()) return;

        int puertoServidor = pedirPuerto(padre, "Puerto del servidor", 8888);
        if (puertoServidor < 0) return;

        List<EntradaRanking> ranking;
        try {
            Registry registro = LocateRegistry.getRegistry(ipServidor, puertoServidor);
            IServicioRanking servicio = (IServicioRanking) registro.lookup(IServicioRanking.NOMBRE_REGISTRO);
            ranking = servicio.obtenerRanking();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(padre, "No se pudo obtener el ranking:\n" + e.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new VistaRanking(padre, ranking).setVisible(true);
    }

    private static int pedirPuerto(JFrame padre, String titulo, int porDefecto) {
        String puerto = JOptionPane.showInputDialog(padre, titulo + ":", String.valueOf(porDefecto));
        if (puerto == null) return -1;
        try {
            return Integer.parseInt(puerto.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(padre, "Puerto inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }
}
