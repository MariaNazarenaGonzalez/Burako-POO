package ar.edu.unlu.poo.burako.vista;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.*;

public class prueba extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea txtSalida;
    private JTextPane textPane;

    public prueba() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        txtSalida.setForeground(Color.BLUE);
        txtSalida.append("prueba /Color.BLUE");
        txtSalida.setForeground(Color.YELLOW);
        txtSalida.append("prueba /Color.YELLOW");

        textPane.setText("Texto normal, texto en rojo, texto normal.");

        /*// Definir el estilo de color rojo
        StyledDocument doc = textPane.getStyledDocument();
        Style estiloRojo = textPane.addStyle("rojo", null);
        StyleConstants.setForeground(estiloRojo, Color.RED);
        doc.setCharacterAttributes(14, 14, estiloRojo, false);*/


        printColor(textPane,"esto es rojo \n",Color.RED);
        printColor(textPane,"esto es azul",Color.BLUE);
        printColor(textPane,"esto es verde",Color.GREEN);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        prueba dialog = new prueba();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    // Método para simular el .append() con color
    public void printColor(JTextPane pane, String texto, Color color) {
        StyledDocument doc = pane.getStyledDocument();

        // Definimos el estilo del nuevo texto
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs, color);

        try {
            // Insertamos al final del documento
            doc.insertString(doc.getLength(), texto, attrs);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
