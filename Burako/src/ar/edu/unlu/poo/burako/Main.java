package ar.edu.unlu.poo.burako;

import ar.edu.unlu.poo.burako.persistencia.PersistenciaService;
import ar.edu.unlu.poo.burako.vista.MenuPrincipal;

import javax.swing.*;

/**
 * Punto de entrada de la aplicación.
 *
 * MODIFICADO (Fase 7 - Menú gráfico):
 * - Ya no contiene wiring de MVC ni lógica de flujo: toda esa
 *   responsabilidad se trasladó a {@link MenuPrincipal} (capa de
 *   presentación), que ahora es la primera ventana que ve el usuario.
 * - Se eliminó por completo el uso de Scanner y System.out del flujo
 *   normal de la aplicación; la única interacción es gráfica (Swing).
 * - Main se limita a construir el único PersistenciaService de la
 *   aplicación (composición mínima de infraestructura) y a mostrar el
 *   menú principal en el Event Dispatch Thread.
 */
public class Main {

    /** Carpeta donde se guardan usuarios.dat, ranking.dat y partidas/. */
    private static final PersistenciaService PERSISTENCIA = new PersistenciaService("data");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuPrincipal(PERSISTENCIA).setVisible(true));
    }
}
