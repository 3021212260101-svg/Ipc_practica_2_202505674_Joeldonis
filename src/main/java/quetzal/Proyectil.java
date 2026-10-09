package quetzal;

import java.awt.Rectangle;

/** Proyectil del jugador: un hilo por disparo que avanza hacia la derecha. */
public class Proyectil extends Thread {
    public static final int ANCHO = 16;
    public static final int ALTO = 5;

    private final PanelJuego panel;
    private volatile int x;
    private final int y;
    private volatile boolean activo = true;

    public Proyectil(PanelJuego panel, int x, int y) {
        this.panel = panel;
        this.x = x;
        this.y = y;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (activo && panel.isEnJuego()) {
            x += 10;
            if (x > PanelJuego.ANCHO) activo = false;
            PanelJuego.dormir(15);
        }
        activo = false;
    }

    public Rectangle getBounds() { return new Rectangle(x, y, ANCHO, ALTO); }
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isActivo() { return activo; }
    public void desactivar() { activo = false; }
}
