package quetzal;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/** Generación de la gráfica (JFreeChart) y exportación HTML usando solo java.io. */
public class Reporte {

    private Reporte() { }

    public static JFreeChart crearGrafica(Partida[] top) {
        DefaultCategoryDataset datos = new DefaultCategoryDataset();
        for (int i = 0; i < top.length; i++) {
            datos.addValue(top[i].getPuntaje(), "Puntaje", "#" + (i + 1) + " " + top[i].getPiloto());
        }
        return ChartFactory.createBarChart("Top 5 mejores puntajes", "Piloto", "Puntaje",
                datos, PlotOrientation.VERTICAL, false, true, false);
    }

    /**
     * Escribe un HTML (listo para imprimir/guardar como PDF desde el navegador)
     * y la gráfica como PNG junto a él.
     */
    public static void exportarHTML(File html, Partida[] top, Partida[] historial, JFreeChart chart)
            throws IOException {
        String nombrePng = html.getName().replaceAll("\\.html?$", "") + "_grafica.png";
        File png = new File(html.getAbsoluteFile().getParentFile(), nombrePng);
        ImageIO.write(chart.createBufferedImage(700, 420), "png", png);

        try (PrintWriter pw = new PrintWriter(new FileWriter(html))) {
            pw.println("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>");
            pw.println("<title>Reporte Quetzal Space Defender</title>");
            pw.println("<style>body{font-family:Arial,sans-serif;margin:30px;color:#222}");
            pw.println("h1{color:#0a0a50}table{border-collapse:collapse;width:100%;margin-bottom:25px}");
            pw.println("th,td{border:1px solid #999;padding:6px;text-align:center}");
            pw.println("th{background:#0a0a50;color:#fff}img{max-width:100%}</style></head><body>");
            pw.println("<h1>Quetzal Space Defender - Reporte de desempeño</h1>");
            pw.println("<h2>Gráfica de mejores puntajes</h2>");
            pw.println("<img src='" + nombrePng + "' alt='Gráfica'>");

            pw.println("<h2>Top 5</h2>");
            pw.println("<table><tr><th>#</th><th>Piloto</th><th>Nave</th><th>Puntaje</th><th>Fecha</th></tr>");
            for (int i = 0; i < top.length; i++) {
                pw.println(fila(i + 1, top[i]));
            }
            pw.println("</table>");

            pw.println("<h2>Historial de partidas</h2>");
            pw.println("<table><tr><th>#</th><th>Piloto</th><th>Nave</th><th>Puntaje</th><th>Fecha</th></tr>");
            for (int i = 0; i < historial.length; i++) {
                pw.println(fila(i + 1, historial[i]));
            }
            pw.println("</table></body></html>");
        }
    }

    private static String fila(int n, Partida p) {
        return "<tr><td>" + n + "</td><td>" + escapar(p.getPiloto()) + "</td><td>"
                + p.getNave().getNombre() + "</td><td>" + p.getPuntaje() + "</td><td>"
                + p.getFecha() + "</td></tr>";
    }

    private static String escapar(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
