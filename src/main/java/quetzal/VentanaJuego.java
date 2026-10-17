package quetzal;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Ventana que contiene el campo de batalla y gestiona el regreso al menú. */
public class VentanaJuego extends JFrame {
    private final MenuPrincipal menu;
    private final PanelJuego panel;
    private final Piloto piloto;
    private final TipoNave nave;

    public VentanaJuego(MenuPrincipal menu, Piloto piloto, TipoNave nave) {
        super("Quetzal Space Defender - " + piloto.getNombre());
        this.menu = menu;
        this.piloto = piloto;
        this.nave = nave;
        this.panel = new PanelJuego(this, piloto, nave);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        add(panel);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                panel.detener(); // abandono: no se guarda la partida
                cerrar();
            }
        });
    }

    public void comenzar() {
        setVisible(true);
        panel.requestFocusInWindow();
        panel.iniciar();
    }

    /** Pantalla de resultados al perder. */
    public void mostrarResultados(int puntaje, int enemigos) {
        JOptionPane.showMessageDialog(this,
                "¡Nave destruida!\n\nPiloto: " + piloto.getNombre()
                        + "\nNave: " + nave
                        + "\nPuntaje final: " + puntaje
                        + "\nEnemigos destruidos: " + enemigos,
                "Resultados", JOptionPane.INFORMATION_MESSAGE);
        cerrar();
    }

    private void cerrar() {
        dispose();
        menu.setVisible(true);
    }
}
