package ar.edu.unlu.poo.burako.vista;

import ar.edu.unlu.poo.burako.controlador.Controlador;
import ar.edu.unlu.poo.burako.modelo.Eventos;
import ar.edu.unlu.poo.burako.modelo.FichaMostrable;
import ar.edu.unlu.poo.burako.modelo.JuegoMostrable;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class VistaConsola extends JFrame implements VistaJuego {
    private final Controlador controlador;
    private JPanel panelPrincipal;
    private JTextPane txtSalida;
    private JTextField txtEntrada;
    private JButton btnEnter;
    private JScrollPane scroll;
    private EstadoVistaConsola estado;
    private int turno;
    private final int miTurno;
    private int[] listafichas={};
    private int f;
    private int j;

    public VistaConsola(Controlador controlador,int turno) {
        setTitle("Burako");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);
        setContentPane(panelPrincipal);
        this.controlador = controlador;
        this.miTurno=turno;
        this.turno=1;
        //inicio estetica
        this.txtSalida.setFont(new Font("Monospaced",Font.BOLD,17));
        this.txtEntrada.setFont(new Font("Monospaced",Font.BOLD,14));
        /*this.txtSalida.setLineWrap(true); // Habilita el salto de línea
        this.txtSalida.setWrapStyleWord(true); // Salto de línea en palabras completas*/
        //fin estetica
        btnEnter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                procesarEntrada(txtEntrada.getText());
                txtEntrada.setText("");
            }
        });
        // Asociar la tecla ENTER
        btnEnter.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "buttonPressed");
        btnEnter.getActionMap().put("buttonPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnEnter.doClick(); // Simula el clic en el botón
            }
        });
        if(miTurno==0){
            this.turno=miTurno;
            mostrarMenu_PrimerTurno();
        }else{
            mostrarMENU_CambioTurno();
        }
    }

    private void procesarEntrada(String entrada) {
        switch (estado) {
            /*
            case MENU_PrimerTurno:
                procesarMenu_PrimerTurno(entrada);
                break;*/
            case MENU_CambioTurno:
                procesarMenu_CambioTurno(entrada);
                break;
            case MENU_CambioTurnoOtro:
                procesarEspera_CambioTurno();
                break;
            case MENU_Jugada:
                procesarMenu_Jugada(entrada);
                break;
            case BajarJuego:
                procesarBajarJuego(entrada);
                break;
            case ApoyarJuego_Juego:
                procesarApoyarJuego(entrada);
                break;
            case ApoyarJuego_Ficha:
                procesarApoyarJuego(entrada);
                break;
            case ApoyarJuego_Pos:
                procesarApoyarJuego(entrada);
                break;
            case AgregarPozo:
                procesarAgregarPozo(entrada);
                break;

       }
    }

    private void procesarEspera_CambioTurno() {
        println("espera "+getNombre(turno)+" esta jugando");
    }

    private void procesarMenu_CambioTurno(String entrada){
            println(entrada);
            switch (entrada){
                case "1":
                    if(this.controlador.agarrarMazo(turno)){
                        println("Agarraste una ficha del mazo.\n" +
                                "Tú atril:\n" );
                        getAtril(miTurno);
                        print("\n");
                        mostrarMenu_Jugada();
                    }else{
                        println("NO FUE POSIBLE AGARRAR UNA FICHA DEL MAZO");
                    }
                    break;
                case "2":
                    if(this.controlador.agarrarPozo(turno)){
                        println("Agarraste una ficha del pozo.\n" +
                                "Tú atril:\n" );
                        getAtril(miTurno);
                        print("\n");
                        mostrarMenu_Jugada();
                    }else{
                        println("NO FUE POSIBLE AGARRAR FICHAS DEL POZO");
                    }
                    break;
                default:
                    println("Opción no válida. Por favor, elija una opción válida.");
            }
    }

    private void procesarMenu_Jugada(String entrada) {
        println(entrada);
        switch (entrada){
            case "1":
                println("Bajar un Juego");
                mostraBajarJuego();
                break;
            case "2":
                println("Apoyar un juego de la Mesa");
                mostrarApoyarJuego();
                break;
            case "3":
                println("Ceder el turno/Poner una Ficha en el pozo");
                mostrarAgregarPozo();
                break;
            default:
                println("Opción no válida. Por favor, elija una opción válida.");
        }
    }

    private void procesarBajarJuego(String entrada){
        if(isNumero(entrada)) {
            print(entrada + ", ");
            int[] nuevoArray = new int[listafichas.length + 1];
            System.arraycopy(listafichas, 0, nuevoArray, 0, listafichas.length);
            nuevoArray[listafichas.length] = Integer.parseInt(entrada);
            listafichas = nuevoArray;
        }else if(entrada.contentEquals(".")){
            println(entrada);
            this.controlador.bajarJuego(turno,listafichas);
            listafichas = new int[0];
            //mostrarMesa();
            mostrarMenu_Jugada();
        }
    }

    private void procesarApoyarJuego(String entrada) {
        println(entrada);
        switch (estado) {
            case ApoyarJuego_Juego:
                if(isNumero(entrada)&& Integer.parseInt(entrada)>=this.controlador.cantJuegos(turno)) {
                    print("eliga una Ficha:");
                    j = Integer.parseInt(entrada);
                    estado = EstadoVistaConsola.ApoyarJuego_Ficha;
                }else{
                    println("opcion invalida");
                }
                break;
            case ApoyarJuego_Ficha:
                if(isNumero(entrada)) {
                    print("eliga la posicion en la que lo decea agregar:");
                    f = Integer.parseInt(entrada);
                    estado = EstadoVistaConsola.ApoyarJuego_Pos;
                }else{
                    println("opcion invalida");
                }
                break;
            case ApoyarJuego_Pos:
                if(isNumero(entrada)) {
                    int p = Integer.parseInt(entrada);
                    this.controlador.apoyarJuego(f, p,turno,j);
                    mostrarMenu_Jugada();
                }else{
                    println("opcion invalida");
                }
                break;
        }
    }

    private void procesarAgregarPozo(String entrada) {

        if(isNumero(entrada)){
            println(entrada);
            int n= Integer.parseInt(entrada);
            this.controlador.agregarPozo(n,turno);
            //mostrarMesa();
        }else{
            print("\n entrada invalida _vuelve a intentar_:");
        }

    }



    private void mostrarMenu_PrimerTurno() {
        //estado=EstadoVistaConsola.MENU_PrimerTurno;
        estado=EstadoVistaConsola.MENU_CambioTurno;
        println("Es turno de "+ getNombre(turno));
        mostrarMesa();
        print("""
                opciones:
                1- Agarrar una ficha del mazo
                2- Agarrar todo el pozo
                ¿Qué quieres hacer?:"""
        );
    }

    private void mostrarMENU_CambioTurno(){
        if(miTurno==(turno+1)%2){
            estado= EstadoVistaConsola.MENU_CambioTurno;
            print("termino el turno de "+ getNombre(turno)+ "\n" + "\n" + "\n" );
            turno=(turno+1)%2;
            println("Es turno de "+ getNombre(turno));
            mostrarMesa();
            print("""
                    opciones:
                    1- Agarrar una ficha del mazo
                    2- Agarrar todo el pozo
                    ¿Qué quieres hacer?:"""
            );
        }else{
            turno=(turno+1)%2;

            estado= EstadoVistaConsola.MENU_CambioTurnoOtro;
            println("Es turno de "+ getNombre(turno));
            println("Esta Jugando...");
        }

    }

    private void mostrarMenu_Jugada() {
        estado=EstadoVistaConsola.MENU_Jugada;
        print("""
                opciones:
                1- Bajar un Juego
                2- Apoyar un juego de la Mesa
                3- Ceder el turno/Poner una Ficha en el pozo
                ¿Qué quieres hacer?:""");
    }

    private void mostraBajarJuego() {
        if(this.controlador.getAtril(turno).size()>=3){
            estado=EstadoVistaConsola.BajarJuego;
            mostrarMesa();
            print("Para seleccionar fichas indique su numero de indice " + "\n" +
                    "separado por ENTER, indique fin de selección con . :");
        }else{
            println("no es posible bajar un juego");
        }
    }

    private void mostrarApoyarJuego() {
        if(this.controlador.cantJuegos(turno) > 0) {
            estado=EstadoVistaConsola.ApoyarJuego_Juego;
            mostrarMesa();
            print("elige un juego:");
        }else {
            println("no es posible apoyar un juego");
        }
    }

    private void mostrarAgregarPozo() {
        estado=EstadoVistaConsola.AgregarPozo;
        mostrarMesa();
        print("elige una ficha:");
    }

    public void mostrarMesa(){
        if(miTurno==turno){
            println("\tLa mesa se ve así:\n" +
                    "El lado de "+getNombre((turno+1)%2));
            getJuegos((turno+1)%2);
            print("\n");
            println("El lado de "+getNombre(turno)+" (tú)" );
            getJuegos(turno);
            print("\n");
            println("El pozo se ve así:" );
            getPozo();
            print("\n");
            println( "Tú atril:" );
            getAtril(miTurno);
            print("\n");
        }else{
            println("\tLa mesa se ve así:\n" +
                    "El lado de "+getNombre(turno));
            getJuegos((turno+1)%2);
            print("\n");
            println("El lado de "+getNombre(miTurno)+" (tú)");
            getJuegos(turno);
            print("\n");
            println("El pozo se ve así:\n" );
            getPozo();
            print("\n");
            print( "Tú atril:\n" );
            getAtril(miTurno);
            print("\n");
        }

    }

    public void mesaje(Eventos eventos) {
        println(eventos.name());
        if(eventos==Eventos.agregarPozo_exitoso){
            mostrarMENU_CambioTurno();
        }
    }


    private void scrollToBottom() {//SCROLL AUTOMATICO
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scroll.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    public void printColor(String texto,String color) {
        StyledDocument doc = txtSalida.getStyledDocument();

        // Definimos el estilo del nuevo texto Rojo,Negro,Amarillo,Azul
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        switch(color){
            case "Rojo":
                StyleConstants.setForeground(attrs, Color.RED);
                break;
            case "Negro":
                StyleConstants.setForeground(attrs, Color.BLACK);
                break;
            case "Amarillo":
                StyleConstants.setForeground(attrs, Color.YELLOW);
                break;
            case "Azul":
                StyleConstants.setForeground(attrs, Color.BLUE);
                break;
            default:
                StyleConstants.setForeground(attrs, Color.BLACK);
        }
        try {
            // Insertamos al final del documento
            doc.insertString(doc.getLength(), texto, attrs);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        scrollToBottom();
    }

    public void print(String texto) {
        printColor(texto,"Negro");
    }

    private void println(String string) {
        print(string + "\n");
    }



    private String getNombre(int turno){
        return this.controlador.getNombre(turno);
    }

    private void getAtril(int miTurno) {
        List<FichaMostrable> listaAtril=controlador.getAtril(miTurno);
        int i=1;

        for(FichaMostrable f: listaAtril){
            print(String.valueOf(i) + "-[");
            printColor(f.getColor().name(),f.getColor().name());
            print("_");
            printColor(f.getNum().name(),f.getColor().name());
            print("]; ");
            i++;
        }
    }

    private void getPozo() {
        List<FichaMostrable> listaPozo=controlador.getPozo();
        int i=1;
        for(FichaMostrable f: listaPozo){
            print(String.valueOf(i) + "-[");
            printColor(f.getColor().name(),f.getColor().name());
            print("_");
            printColor(f.getNum().name(),f.getColor().name());
            print("]; ");
            i++;
        }
    }

    private void getJuegos(int jugador) {
        int i=1;
        List<JuegoMostrable> listaJuegos = controlador.getJuegos(jugador);
        for(JuegoMostrable j:listaJuegos){
            print("   "+ i +"-");
            printJuego(j);
            print("\n");
            i++;
        }
    }

    public void printJuego(JuegoMostrable j) {
        List<FichaMostrable> listaJuego=j.getJuego();
        for(FichaMostrable f: listaJuego){
            print("[");
            printColor(f.getColor().name(),f.getColor().name());
            print("_");
            printColor(f.getNum().name(),f.getColor().name());
            print("] ");
        }
    }

    private boolean isNumero(String entrada) {
        try {
            int numero = Integer.parseInt(entrada);
            return numero >= 1 && numero <= this.controlador.getAtril(turno).size();
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
