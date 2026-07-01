package ar.edu.unlu.poo.burako;
import ar.edu.unlu.poo.burako.controlador.Controlador;
import ar.edu.unlu.poo.burako.modelo.Burako;
import ar.edu.unlu.poo.burako.modelo.EstadoTurno;
import ar.edu.unlu.poo.burako.modelo.IBurako;
import ar.edu.unlu.poo.burako.persistencia.ObservadorPersistencia;
import ar.edu.unlu.poo.burako.persistencia.PartidaGuardada;
import ar.edu.unlu.poo.burako.persistencia.PersistenciaService;
import ar.edu.unlu.poo.burako.persistencia.Usuario;
import ar.edu.unlu.poo.burako.vista.VistaConsola;
import ar.edu.unlu.poo.burako.vista.VistaGrafica;

import javax.swing.*;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Punto de entrada de la aplicación.
 *
 * MODIFICADO (Fase 4):
 * - La variable local 'burako' ahora está declarada como IBurako.
 *   Burako concreto sigue siendo instanciado aquí (necesario para la construcción),
 *   pero toda la wiring posterior usa la interfaz.
 * - crearVista recibe IBurako para reforzar que el Controlador no necesita
 *   conocer la implementación concreta.
 *
 * MODIFICADO (Fase 6 - Persistencia):
 * - Antes de iniciar la partida se registran/recuperan los Usuario de ambos
 *   jugadores (PersistenciaService.obtenerOcrearUsuario) y, si existe una
 *   partida guardada entre ambos, se ofrece continuarla en lugar de crear
 *   una nueva (PersistenciaService.continuarPartida).
 * - Se agrega un ObservadorPersistencia -además del Controlador- para que,
 *   al finalizar la partida, se actualicen automáticamente las
 *   estadísticas de Usuario y el Ranking. Esto reutiliza el mecanismo de
 *   Observer ya existente (burako.agregarObservador admite múltiples
 *   observadores); no se modifica Observador, Observado ni Controlador.
 * - Se agrega un shutdown hook que guarda el estado de la partida en curso
 *   al cerrar la aplicación, para poder continuarla más adelante.
 * - crearVista() y preguntarTipoVista() quedan exactamente como en la
 *   Fase 4: ninguna de las adiciones de esta fase toca la creación de la
 *   Vista ni del Controlador.
 */
public class Main {

    /** Carpeta donde se guardan usuarios.dat, ranking.dat y partidas/. */
    private static final PersistenciaService PERSISTENCIA = new PersistenciaService("data");

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);

        Usuario usuario1 = obtenerUsuario(teclado, "Jugador 1");
        Usuario usuario2 = obtenerUsuario(teclado, "Jugador 2");

        PartidaGuardada partidaPrevia = buscarPartidaAContinuar(teclado, usuario1, usuario2);

        Burako burakoConcreto;
        String idPartidaActual;
        if (partidaPrevia != null) {
            burakoConcreto = partidaPrevia.getEstado();
            idPartidaActual = partidaPrevia.getId();
            System.out.println("Continuando partida guardada (" + idPartidaActual + ")...");
        } else {
            burakoConcreto = new Burako();
            burakoConcreto.setNombres(usuario1.getNombre(), usuario2.getNombre());
            idPartidaActual = UUID.randomUUID().toString();
        }

        IBurako burako = burakoConcreto;
        burako.agregarObservador(new ObservadorPersistencia(
                PERSISTENCIA, burakoConcreto, usuario1, usuario2, idPartidaActual));
        registrarGuardadoAlCerrar(burakoConcreto, usuario1, usuario2, idPartidaActual);

        SwingUtilities.invokeLater(() -> {
            boolean usarConsola = preguntarTipoVista();
            crearVista(burako, 0, usarConsola);
            crearVista(burako, 1, usarConsola);
        });
    }

    // ── Persistencia (Fase 6) ───────────────────────────────────────────────

    /** Registra un Usuario nuevo o recupera el existente con el nombre ingresado. */
    private static Usuario obtenerUsuario(Scanner teclado, String etiqueta) {
        System.out.print("Nombre de " + etiqueta + ": ");
        String nombre = teclado.nextLine().trim();
        if (nombre.isEmpty()) {
            nombre = etiqueta;
        }
        return PERSISTENCIA.obtenerOcrearUsuario(nombre);
    }

    /** Si existe una partida guardada entre ambos usuarios, ofrece continuarla. */
    private static PartidaGuardada buscarPartidaAContinuar(Scanner teclado, Usuario u1, Usuario u2) {
        List<PartidaGuardada> guardadas = PERSISTENCIA.listarPartidasDe(u1);
        guardadas.removeIf(p -> !p.participaUsuario(u2.getId()));
        if (guardadas.isEmpty()) {
            return null;
        }

        PartidaGuardada ultima = guardadas.get(guardadas.size() - 1);
        System.out.print("Hay una partida guardada entre " + u1.getNombre() + " y " + u2.getNombre()
                + " (" + ultima.getFechaGuardado() + "). ¿Continuarla? (s/n): ");
        String respuesta = teclado.nextLine().trim();
        return respuesta.equalsIgnoreCase("s") ? ultima : null;
    }

    /** Guarda el estado de la partida al cerrar la aplicación, si todavía no terminó. */
    private static void registrarGuardadoAlCerrar(Burako burako, Usuario u1, Usuario u2, String idPartida) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (burako.getEstadoTurno() != EstadoTurno.PARTIDA_TERMINADA) {
                PERSISTENCIA.guardarPartida(idPartida, burako, u1, u2);
            }
        }));
    }

    // ── Fase 4 (sin cambios) ────────────────────────────────────────────────

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
