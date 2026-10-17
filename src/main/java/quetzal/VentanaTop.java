package quetzal;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

/** Top de puntajes: gráfica JFreeChart, historial en tabla y exportación. */
public class VentanaTop extends JDialog {
    private final Partida[] top;
    private final Partida[] historial;
    private final JFreeChart chart;

    public VentanaTop(JFrame padre) {
        super(padre, "Top de Puntajes", true);
        top = GestorDatos.getTop(5);
        historial = GestorDatos.getPartidas();
        chart = Reporte.crearGrafica(top);

        setLayout(new BorderLayout(5, 5));
        add(new ChartPanel(chart), BorderLayout.CENTER);

        // Tabla con matrices (sin colecciones)
        String[] columnas = {"#", "Piloto", "Nave", "Puntaje", "Enemigos", "Fecha"};
        Object[][] filas = new Object[historial.length][6];
        for (int i = 0; i < historial.length; i++) {
            Partida p = historial[i];
            filas[i][0] = i + 1;
            filas[i][1] = p.getPiloto();
            filas[i][2] = p.getNave().getNombre();
            filas[i][3] = p.getPuntaje();
            filas[i][4] = p.getEnemigosDestruidos();
            filas[i][5] = p.getFecha();
        }
        JScrollPane scroll = new JScrollPane(new JTable(filas, columnas));
        scroll.setPreferredSize(new Dimension(800, 170));
        add(scroll, BorderLayout.SOUTH);

        JButton btnExportar = new JButton("Exportar reporte HTML/PDF");
        btnExportar.addActionListener(e -> exportar());
        JPanel norte = new JPanel();
        norte.add(btnExportar);
        add(norte, BorderLayout.NORTH);

        setSize(850, 640);
        setLocationRelativeTo(padre);
    }

    private void exportar() {
        if (historial.length == 0) {
            JOptionPane.showMessageDialog(this, "Aún no hay partidas para exportar.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("reporte_quetzal.html"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File archivo = fc.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".html")) {
            archivo = new File(archivo.getAbsolutePath() + ".html");
        }
        try {
            Reporte.exportarHTML(archivo, top, historial, chart);
            JOptionPane.showMessageDialog(this, "Reporte exportado en:\n" + archivo.getAbsolutePath()
                    + "\n\nÁbrelo en el navegador y usa Imprimir > Guardar como PDF.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
