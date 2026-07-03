package ar.edu.unlu.poo.burako;

import ar.edu.unlu.poo.burako.vista.MenuPrincipal;

import javax.swing.*;

/**
 * Punto de entrada de la aplicación CLIENTE.
 *
 * MODIFICADO (Fase 7 - Menú gráfico):
 * - Ya no contiene wiring de MVC ni lógica de flujo: toda esa
 *   responsabilidad se trasladó a MenuPrincipal (capa de presentación).
 *
 * MODIFICADO (Fase 9 - Integración RMIMVC):
 * - Ya no crea PersistenciaService: la persistencia ahora vive
 *   exclusivamente en el servidor (ver servidor.AppServidor). Este Main
 *   es el punto de entrada de UN cliente (Vista + Controlador + proxy
 *   remoto), no del servidor.
 * - Para iniciar el servidor, ejecutar servidor.AppServidor en un proceso
 *   separado (ver README de la entrega).
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuPrincipal().setVisible(true));
    }
}
