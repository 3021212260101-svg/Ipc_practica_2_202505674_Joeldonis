package quetzal;

import javax.swing.SwingUtilities;

/** Punto de entrada: solo lanza el menú principal en el hilo de Swing. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuPrincipal().setVisible(true));
    }
}
