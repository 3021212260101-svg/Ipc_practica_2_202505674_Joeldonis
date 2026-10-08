package quetzal;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

/** Menú principal: Jugar, Crear Piloto, Top de Puntajes y Salir. */
public class MenuPrincipal extends JFrame {

    public MenuPrincipal() {
        super("Quetzal Space Defender");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 430);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel contenido = new JPanel(new GridLayout(5, 1, 10, 10));
        contenido.setBorder(BorderFactory.createEmptyBorder(25, 50, 25, 50));
        contenido.setBackground(new Color(10, 10, 40));

        JLabel titulo = new JLabel("QUETZAL SPACE DEFENDER", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);

        JButton btnJugar = crearBoton("Jugar");
        JButton btnPiloto = crearBoton("Crear Piloto");
        JButton btnTop = crearBoton("Top de Puntajes");
        JButton btnSalir = crearBoton("Salir");

        btnJugar.addActionListener(e -> jugar());
        btnPiloto.addActionListener(e -> crearPiloto());
        btnTop.addActionListener(e -> new VentanaTop(this).setVisible(true));
        btnSalir.addActionListener(e -> System.exit(0));

        contenido.add(titulo);
        contenido.add(btnJugar);
        contenido.add(btnPiloto);
        contenido.add(btnTop);
        contenido.add(btnSalir);
        setContentPane(contenido);
    }

    private JButton crearBoton(String texto) {
        JButton b = new JButton(texto);
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setFocusPainted(false);
        return b;
    }

    /** Selecciona piloto y nave (la nave define la dificultad) e inicia la partida. */
    private void jugar() {
        Piloto[] pilotos = GestorDatos.getPilotos();
        if (pilotos.length == 0) {
            JOptionPane.showMessageDialog(this, "Primero debes crear un piloto.",
                    "Sin pilotos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JComboBox<Piloto> cbPiloto = new JComboBox<>(pilotos);
        JComboBox<TipoNave> cbNave = new JComboBox<>(TipoNave.values());
        cbNave.setSelectedItem(pilotos[0].getNave());
        cbPiloto.addActionListener(e ->
                cbNave.setSelectedItem(((Piloto) cbPiloto.getSelectedItem()).getNave()));

        JPanel p = new JPanel(new GridLayout(4, 1, 5, 5));
        p.add(new JLabel("Piloto:"));
        p.add(cbPiloto);
        p.add(new JLabel("Modelo de nave (dificultad):"));
        p.add(cbNave);

        int op = JOptionPane.showConfirmDialog(this, p, "Nueva partida",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op == JOptionPane.OK_OPTION) {
            Piloto piloto = (Piloto) cbPiloto.getSelectedItem();
            TipoNave nave = (TipoNave) cbNave.getSelectedItem();
            piloto.setNave(nave);
            setVisible(false);
            new VentanaJuego(this, piloto, nave).comenzar();
        }
    }

    /** Registro de piloto con validaciones. */
    private void crearPiloto() {
        JTextField txtNombre = new JTextField(15);
        JComboBox<TipoNave> cbNave = new JComboBox<>(TipoNave.values());
        JPanel p = new JPanel(new GridLayout(4, 1, 5, 5));
        p.add(new JLabel("Nombre del piloto (máx. 15 caracteres):"));
        p.add(txtNombre);
        p.add(new JLabel("Modelo de nave:"));
        p.add(cbNave);

        int op = JOptionPane.showConfirmDialog(this, p, "Crear piloto",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op != JOptionPane.OK_OPTION) return;

        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            error("El nombre no puede estar vacío.");
        } else if (nombre.length() > 15) {
            error("El nombre no puede superar 15 caracteres.");
        } else if (!nombre.matches("[A-Za-z0-9_ ]+")) {
            error("Usa solo letras, números, espacios o guion bajo.");
        } else if (!GestorDatos.agregarPiloto(nombre, (TipoNave) cbNave.getSelectedItem())) {
            error("Ya existe un piloto con ese nombre.");
        } else {
            JOptionPane.showMessageDialog(this, "Piloto \"" + nombre + "\" creado correctamente.");
        }
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validación", JOptionPane.ERROR_MESSAGE);
    }
}
