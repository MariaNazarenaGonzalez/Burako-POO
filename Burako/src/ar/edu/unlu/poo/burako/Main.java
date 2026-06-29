package ar.edu.unlu.poo.burako;
import ar.edu.unlu.poo.burako.controlador.Controlador;
import ar.edu.unlu.poo.burako.modelo.Burako;
import ar.edu.unlu.poo.burako.modelo.IBurako;
import ar.edu.unlu.poo.burako.vista.VistaConsola;
import ar.edu.unlu.poo.burako.vista.VistaGrafica;

import javax.swing.*;

/**
 * Punto de entrada de la aplicación.
 *
 * MODIFICADO (Fase 4):
 * - La variable local 'burako' ahora está declarada como IBurako.
 *   Burako concreto sigue siendo instanciado aquí (necesario para la construcción),
 *   pero toda la wiring posterior usa la interfaz.
 * - crearVista recibe IBurako para reforzar que el Controlador no necesita
 *   conocer la implementación concreta.
 */
public class Main {
    public static void main(String[] args) {
        IBurako burako = new Burako();
        burako.setNombres("Jugador1", "Jugador2");

        SwingUtilities.invokeLater(() -> {
            boolean usarConsola = preguntarTipoVista();
            crearVista(burako, 0, usarConsola);
            crearVista(burako, 1, usarConsola);
        });
    }

    private static boolean preguntarTipoVista() {
        String[] opciones = {"Grafica", "Consola"};
        int seleccion = JOptionPane.showOptionDialog(
                null,
                "Que tipo de vista quieres usar?",
                "Burako",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );
        return seleccion == 1;
    }

    private static void crearVista(IBurako burako, int jugador, boolean usarConsola) {
        var controlador = new Controlador(burako);
        burako.agregarObservador(controlador);
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
