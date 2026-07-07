package ar.edu.unlu.poo.burako.vista;

import ar.edu.unlu.poo.burako.persistencia.EntradaRanking;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
}
