package quetzal;

import java.awt.Rectangle;

/**
 * Enemigo, asteroide, núcleo o cápsula. Cada instancia es un hilo independiente
 * que actualiza su posición (x, y) desde el borde derecho hacia el izquierdo.
 */
public class ObjetoEspacial extends Thread {
    public enum Tipo { ENEMIGO, ASTEROIDE, NUCLEO, CAPSULA }

    private final PanelJuego panel;
    private final Tipo tipo;
    private volatile int x;
    private volatile int y;
    private final int ancho;
    private final int alto;
    private final int velocidad;
    private int dy; // deriva vertical (solo enemigos)
    private volatile boolean activo = true;

    public ObjetoEspacial(PanelJuego panel, Tipo tipo, int x, int y, int ancho, int alto, int velocidad) {
        this.panel = panel;
        this.tipo = tipo;
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
        this.velocidad = velocidad;
        this.dy = (tipo == Tipo.ENEMIGO) ? (Math.random() < 0.5 ? -1 : 1) : 0;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (activo && panel.isEnJuego()) {
            x -= velocidad;
            if (dy != 0) {
                y += dy;
                if (y < 0 || y > PanelJuego.ALTO - alto) dy = -dy;
            }
            if (x + ancho < 0) activo = false; // salió por el X mínimo
            PanelJuego.dormir(20);
        }
        activo = false;
    }

    public Rectangle getBounds() { return new Rectangle(x, y, ancho, alto); }
    public Tipo getTipo() { return tipo; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }
    public boolean isActivo() { return activo; }
    public void desactivar() { activo = false; }
}
