package ar.edu.unlu.poo.burako;

import ar.edu.unlu.poo.burako.vista.MenuPrincipal;

import javax.swing.*;

/**
 * Punto de entrada de la aplicación cliente.
 *
 * Inicializa la interfaz gráfica y muestra el menú principal,
 * desde el cual el usuario puede acceder a las distintas
 * funcionalidades del sistema.
 */
public class Main {
    /**
     * Inicia la ejecución de la aplicación.
     *
     * La interfaz gráfica se crea dentro del hilo de despacho de
     * eventos de Swing para garantizar un funcionamiento seguro
     * de los componentes gráficos.
     *
     * @param args argumentos recibidos desde la línea de comandos.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuPrincipal().setVisible(true));
    }
}
