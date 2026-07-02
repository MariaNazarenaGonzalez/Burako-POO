package ar.edu.unlu.poo.burako.vista;

import ar.edu.unlu.poo.burako.controlador.Controlador;
import ar.edu.unlu.poo.burako.modelo.Burako;
import ar.edu.unlu.poo.burako.modelo.EstadoTurno;
import ar.edu.unlu.poo.burako.modelo.IBurako;
import ar.edu.unlu.poo.burako.persistencia.EntradaRanking;
import ar.edu.unlu.poo.burako.persistencia.ObservadorPersistencia;
import ar.edu.unlu.poo.burako.persistencia.PartidaGuardada;
import ar.edu.unlu.poo.burako.persistencia.PersistenciaService;
import ar.edu.unlu.poo.burako.persistencia.Usuario;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.UUID;

/**
 * Menú principal de la aplicación, completamente gráfico (Swing).
 * Es la primera ventana que ve el usuario, antes de que exista cualquier
 * partida. Ofrece cuatro opciones: Nueva partida, Cargar partida,
 * Ver ranking y Salir.
 *
 * Responsabilidad: orquestar la navegación y la construcción de la
 * arquitectura MVC (Burako + Controlador + Vista) a partir de las
 * elecciones del usuario, delegando:
 * - toda regla de juego al modelo (Burako, a través de IBurako),
 * - toda lectura/escritura de datos a {@link PersistenciaService}.
 * No contiene lógica de negocio propia: solo arma diálogos, valida
 * entradas de forma trivial (nombre no vacío) y conecta los objetos ya
 * existentes, exactamente como antes hacía Main.main().
 *
 * NOTA DE DISEÑO: esta clase, al vivir en la capa de presentación,
 * depende de persistencia (para listar partidas/ranking y registrar
 * usuarios) y de controlador/modelo (para construir la partida). Esa
 * dirección de dependencia es la esperada: es la capa de presentación la
 * que orquesta al resto, nunca al revés. El modelo (Burako y
 * colaboradores) sigue sin conocer nada de persistencia, vista ni
 * controlador, tal como en las fases anteriores.
 */
public class MenuPrincipal extends JFrame {

    private final PersistenciaService persistencia;

    public MenuPrincipal(PersistenciaService persistencia) {
        super("Burako");
        this.persistencia = persistencia;
        construirVentana();
    }

    private void construirVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1, 12, 12));

        JButton botonNueva = new JButton("Nueva partida");
        JButton botonCargar = new JButton("Cargar partida");
        JButton botonRanking = new JButton("Ver ranking");
        JButton botonSalir = new JButton("Salir");

        botonNueva.addActionListener(e -> flujoNuevaPartida());
        botonCargar.addActionListener(e -> flujoCargarPartida());
        botonRanking.addActionListener(e -> flujoVerRanking());
        botonSalir.addActionListener(e -> System.exit(0));

        JPanel contenido = new JPanel(new GridLayout(4, 1, 12, 12));
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));
        contenido.add(botonNueva);
        contenido.add(botonCargar);
        contenido.add(botonRanking);
        contenido.add(botonSalir);
        setContentPane(contenido);

        setSize(340, 280);
        setLocationRelativeTo(null);
    }

    // ── Opción 1: Nueva partida ─────────────────────────────────────────────

    private void flujoNuevaPartida() {
        if (!confirmarDosJugadores()) {
            return;
        }

        String nombre1 = pedirNombreJugador("Jugador 1");
        if (nombre1 == null) {
            return;
        }
        String nombre2 = pedirNombreDistinto(nombre1);
        if (nombre2 == null) {
            return;
        }

        Usuario usuario1 = persistencia.obtenerOcrearUsuario(nombre1);
        Usuario usuario2 = persistencia.obtenerOcrearUsuario(nombre2);

        Burako burakoConcreto = new Burako();
        burakoConcreto.setNombres(usuario1.getNombre(), usuario2.getNombre());
        String idPartida = UUID.randomUUID().toString();

        iniciarPartida(burakoConcreto, usuario1, usuario2, idPartida);
    }

    /**
     * Pregunta la cantidad de jugadores. El modelo actual (Burako) solo
     * soporta 2 jugadores, por lo que si se elige 4 se informa la
     * limitación y se continúa con 2. No se modifica el modelo para
     * soportar 4 jugadores en esta fase.
     * @return true si corresponde continuar con la creación de la partida.
     */
    private boolean confirmarDosJugadores() {
        Object[] opciones = {"2 Jugadores", "4 Jugadores"};
        int seleccion = JOptionPane.showOptionDialog(
                this,
                "¿Cuántos jugadores participarán?",
                "Nueva partida",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );
        if (seleccion == JOptionPane.CLOSED_OPTION) {
            return false;
        }
        if (seleccion == 1) {
            JOptionPane.showMessageDialog(
                    this,
                    "El modelo actual soporta únicamente 2 jugadores.\nSe continuará con una partida de 2 jugadores.",
                    "Aviso",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
        return true;
    }

    private String pedirNombreDistinto(String nombreExistente) {
        while (true) {
            String nombre = pedirNombreJugador("Jugador 2");
            if (nombre == null) {
                return null;
            }
            if (nombre.equalsIgnoreCase(nombreExistente)) {
                JOptionPane.showMessageDialog(this,
                        "El Jugador 2 debe tener un nombre distinto al Jugador 1.",
                        "Burako", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            return nombre;
        }
    }

    private String pedirNombreJugador(String etiqueta) {
        while (true) {
            String nombre = JOptionPane.showInputDialog(
                    this, "Nombre de " + etiqueta + ":", "Nueva partida", JOptionPane.PLAIN_MESSAGE);
            if (nombre == null) {
                return null; // el usuario canceló
            }
            nombre = nombre.trim();
            if (!nombre.isEmpty()) {
                return nombre;
            }
            JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío.",
                    "Burako", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ── Opción 2: Cargar partida ────────────────────────────────────────────

    private void flujoCargarPartida() {
        List<PartidaGuardada> guardadas = persistencia.listarPartidasGuardadas();
        if (guardadas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay partidas guardadas.",
                    "Cargar partida", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        PartidaGuardada seleccionada = (PartidaGuardada) JOptionPane.showInputDialog(
                this,
                "Seleccione una partida guardada:",
                "Cargar partida",
                JOptionPane.QUESTION_MESSAGE,
                null,
                guardadas.toArray(),
                guardadas.get(0)
        );
        if (seleccionada == null) {
            return;
        }

        Usuario usuario1 = persistencia.obtenerOcrearUsuario(seleccionada.getNombreUsuario1());
        Usuario usuario2 = persistencia.obtenerOcrearUsuario(seleccionada.getNombreUsuario2());

        iniciarPartida(seleccionada.getEstado(), usuario1, usuario2, seleccionada.getId());
    }

    // ── Construcción común de MVC (nueva o cargada) ─────────────────────────

    /**
     * Registra la persistencia automática y crea las vistas de ambos
     * jugadores para el estado de partida dado (nuevo o recuperado).
     * Idéntico wiring de MVC/Observer usado en fases anteriores.
     */
    private void iniciarPartida(Burako burakoConcreto, Usuario usuario1, Usuario usuario2, String idPartida) {
        IBurako burako = burakoConcreto;
        burako.agregarObservador(new ObservadorPersistencia(
                persistencia, burakoConcreto, usuario1, usuario2, idPartida));
        registrarGuardadoAlCerrar(burakoConcreto, usuario1, usuario2, idPartida);

        boolean consolaJugador1 = preguntarTipoVista(usuario1.getNombre());
        boolean consolaJugador2 = preguntarTipoVista(usuario2.getNombre());
        crearVista(burako, 0, consolaJugador1);
        crearVista(burako, 1, consolaJugador2);

        setVisible(false);
    }

    private boolean preguntarTipoVista(String nombreJugador) {
        Object[] opciones = {"Swing", "Consola"};
        int seleccion = JOptionPane.showOptionDialog(
                this,
                "¿Qué tipo de vista usará " + nombreJugador + "?",
                "Selección de vista",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );
        return seleccion == 1;
    }

    private void crearVista(IBurako burako, int jugador, boolean usarConsola) {
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

    /** Guarda el estado de la partida al cerrar la aplicación, si todavía no terminó. */
    private void registrarGuardadoAlCerrar(Burako burako, Usuario u1, Usuario u2, String idPartida) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (burako.getEstadoTurno() != EstadoTurno.PARTIDA_TERMINADA) {
                persistencia.guardarPartida(idPartida, burako, u1, u2);
            }
        }));
    }

    // ── Opción 3: Ver ranking ────────────────────────────────────────────────

    private void flujoVerRanking() {
        List<EntradaRanking> ranking = persistencia.obtenerRanking();
        new VistaRanking(this, ranking).setVisible(true);
    }
}
