package quetzal;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Campo de batalla (side scroller). Hilos del juego:
 *  - movimiento del jugador (sleep según nave)
 *  - disparo del jugador (sleep = cadencia de la nave)
 *  - generador de enemigos/asteroides/premios
 *  - lógica (colisiones + repintado)
 * Además, cada enemigo/objeto y cada proyectil es su propio hilo.
 */
public class PanelJuego extends JPanel implements KeyListener {
    public static final int ANCHO = 900;
    public static final int ALTO = 560;
    private static final int JW = 44;   // ancho de la nave del jugador
    private static final int JH = 28;   // alto de la nave del jugador
    private static final int PASO = 4;  // píxeles por tick de movimiento
    private static final int PUNTOS_NUCLEO = 150;
    private static final int PUNTOS_CAPSULA = 10;
    private static final int PUNTOS_ENEMIGO = 20;
    private static final int MS_LENTO = 2000;

    private final VentanaJuego ventana;
    private final Piloto piloto;
    private final TipoNave nave;

    private final Object lock = new Object();
    private final ObjetoEspacial[] objetos = new ObjetoEspacial[40];
    private final Proyectil[] proyectiles = new Proyectil[30];
    private final int[] estrellaX = new int[70];
    private final int[] estrellaY = new int[70];
    private final int[] estrellaV = new int[70];
    private final Random rnd = new Random();

    private volatile int jx = 60;
    private volatile int jy = ALTO / 2;
    private volatile boolean arriba, abajo, izq, der, disparando;
    private volatile boolean enJuego = false;
    private volatile long lentoHasta = 0;
    private volatile int puntaje = 0;
    private volatile int enemigosDestruidos = 0;
    private boolean finalizada = false;

    public PanelJuego(VentanaJuego ventana, Piloto piloto, TipoNave nave) {
        this.ventana = ventana;
        this.piloto = piloto;
        this.nave = nave;
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        for (int i = 0; i < estrellaX.length; i++) {
            estrellaX[i] = rnd.nextInt(ANCHO);
            estrellaY[i] = rnd.nextInt(ALTO);
            estrellaV[i] = 1 + rnd.nextInt(3);
        }
    }

    // ------------------------------------------------------------ control
    public void iniciar() {
        enJuego = true;
        crearHilo(this::bucleMovimiento);
        crearHilo(this::bucleDisparo);
        crearHilo(this::bucleGenerador);
        crearHilo(this::bucleLogica);
    }

    private void crearHilo(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();
    }

    /** Detiene todos los hilos sin guardar partida (cierre de ventana). */
    public void detener() {
        enJuego = false;
        desactivarTodo();
    }

    private void desactivarTodo() {
        synchronized (lock) {
            for (int i = 0; i < objetos.length; i++) {
                if (objetos[i] != null) objetos[i].desactivar();
            }
            for (int i = 0; i < proyectiles.length; i++) {
                if (proyectiles[i] != null) proyectiles[i].desactivar();
            }
        }
    }

    public boolean isEnJuego() { return enJuego; }

    public static void dormir(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean estaLento() { return System.currentTimeMillis() < lentoHasta; }

    // ------------------------------------------------------------ hilos
    /** Hilo de movimiento: el sleep depende de la nave (y se triplica si está ralentizada). */
    private void bucleMovimiento() {
        while (enJuego) {
            if (izq) jx -= PASO;
            if (der) jx += PASO;
            if (arriba) jy -= PASO;
            if (abajo) jy += PASO;
            jx = Math.max(0, Math.min(ANCHO - JW, jx));
            jy = Math.max(0, Math.min(ALTO - JH, jy));
            int base = nave.getSleepMovimiento();
            dormir(estaLento() ? base * 3 : base);
        }
    }

    /** Hilo de disparo: cadencia definida por la nave. */
    private void bucleDisparo() {
        while (enJuego) {
            if (disparando) {
                disparar();
                dormir(nave.getCooldownDisparo());
            } else {
                dormir(10);
            }
        }
    }

    private void disparar() {
        synchronized (lock) {
            for (int i = 0; i < proyectiles.length; i++) {
                if (proyectiles[i] == null || !proyectiles[i].isActivo()) {
                    proyectiles[i] = new Proyectil(this, jx + JW, jy + JH / 2 - 2);
                    proyectiles[i].start();
                    return;
                }
            }
        }
    }

    /** Hilo generador: crea objetos en el borde derecho (X máximo). */
    private void bucleGenerador() {
        while (enJuego) {
            generar();
            dormir(600 + rnd.nextInt(600));
        }
    }

    private void generar() {
        int r = rnd.nextInt(100);
        int y = 10 + rnd.nextInt(ALTO - 70);
        ObjetoEspacial nuevo;
        if (r < 45) {
            nuevo = new ObjetoEspacial(this, ObjetoEspacial.Tipo.ENEMIGO, ANCHO, y, 40, 28, 3 + rnd.nextInt(3));
        } else if (r < 75) {
            nuevo = new ObjetoEspacial(this, ObjetoEspacial.Tipo.ASTEROIDE, ANCHO, y, 44, 44, 2 + rnd.nextInt(2));
        } else if (r < 93) {
            nuevo = new ObjetoEspacial(this, ObjetoEspacial.Tipo.CAPSULA, ANCHO, y, 20, 20, 3);
        } else {
            nuevo = new ObjetoEspacial(this, ObjetoEspacial.Tipo.NUCLEO, ANCHO, y, 28, 28, 3);
        }
        synchronized (lock) {
            for (int i = 0; i < objetos.length; i++) {
                if (objetos[i] == null || !objetos[i].isActivo()) {
                    objetos[i] = nuevo;
                    nuevo.start();
                    return;
                }
            }
        }
    }

    /** Hilo de lógica: fondo, colisiones y repintado (~60 FPS). */
    private void bucleLogica() {
        while (enJuego) {
            for (int i = 0; i < estrellaX.length; i++) {
                estrellaX[i] -= estrellaV[i];
                if (estrellaX[i] < 0) {
                    estrellaX[i] = ANCHO;
                    estrellaY[i] = rnd.nextInt(ALTO);
                }
            }
            boolean choque = revisarColisiones();
            repaint();
            if (choque) {
                finalizarPartida();
                return;
            }
            dormir(16);
        }
    }

    /** Devuelve true si el jugador chocó de forma fatal. */
    private boolean revisarColisiones() {
        boolean choque = false;
        Rectangle nave = new Rectangle(jx + 4, jy + 4, JW - 8, JH - 8);
        synchronized (lock) {
            for (int i = 0; i < objetos.length; i++) {
                ObjetoEspacial o = objetos[i];
                if (o == null || !o.isActivo()) continue;
                Rectangle b = o.getBounds();

                // Proyectil vs enemigo
                if (o.getTipo() == ObjetoEspacial.Tipo.ENEMIGO) {
                    for (int k = 0; k < proyectiles.length; k++) {
                        Proyectil p = proyectiles[k];
                        if (p != null && p.isActivo() && p.getBounds().intersects(b)) {
                            p.desactivar();
                            o.desactivar();
                            puntaje += PUNTOS_ENEMIGO;
                            enemigosDestruidos++;
                            break;
                        }
                    }
                    if (!o.isActivo()) continue;
                }

                // Jugador vs objeto
                if (nave.intersects(b)) {
                    switch (o.getTipo()) {
                        case ENEMIGO:
                            choque = true;
                            break;
                        case ASTEROIDE:
                            // Si ya está ralentizado, el asteroide no hace daño.
                            if (!estaLento()) {
                                lentoHasta = System.currentTimeMillis() + MS_LENTO;
                                o.desactivar();
                            }
                            break;
                        case CAPSULA:
                            puntaje += PUNTOS_CAPSULA;
                            o.desactivar();
                            break;
                        case NUCLEO:
                            puntaje += PUNTOS_NUCLEO;
                            o.desactivar();
                            destruirEnemigosVisibles();
                            break;
                    }
                }
            }
        }
        return choque;
    }

    /** Efecto del Núcleo de Energía: elimina todos los enemigos visibles. */
    private void destruirEnemigosVisibles() {
        for (int i = 0; i < objetos.length; i++) {
            ObjetoEspacial e = objetos[i];
            if (e != null && e.isActivo() && e.getTipo() == ObjetoEspacial.Tipo.ENEMIGO
                    && e.getX() < ANCHO) {
                e.desactivar();
                enemigosDestruidos++;
            }
        }
    }

    private void finalizarPartida() {
        synchronized (this) {
            if (finalizada) return;
            finalizada = true;
        }
        enJuego = false;
        desactivarTodo();
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        GestorDatos.agregarPartida(new Partida(piloto.getNombre(), nave, puntaje, enemigosDestruidos, fecha));
        final int pts = puntaje;
        final int kills = enemigosDestruidos;
        SwingUtilities.invokeLater(() -> ventana.mostrarResultados(pts, kills));
    }

    // ------------------------------------------------------------ dibujo
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(5, 5, 25));
        g.fillRect(0, 0, ANCHO, ALTO);
        g.setColor(Color.WHITE);
        for (int i = 0; i < estrellaX.length; i++) {
            g.fillRect(estrellaX[i], estrellaY[i], estrellaV[i], estrellaV[i]);
        }

        synchronized (lock) {
            for (int i = 0; i < objetos.length; i++) {
                if (objetos[i] != null && objetos[i].isActivo()) dibujarObjeto(g, objetos[i]);
            }
            g.setColor(Color.YELLOW);
            for (int i = 0; i < proyectiles.length; i++) {
                Proyectil p = proyectiles[i];
                if (p != null && p.isActivo()) g.fillRoundRect(p.getX(), p.getY(), Proyectil.ANCHO, Proyectil.ALTO, 4, 4);
            }
        }

        // Nave del jugador (apunta a la derecha)
        Polygon pol = new Polygon(
                new int[]{jx, jx + JW, jx},
                new int[]{jy, jy + JH / 2, jy + JH}, 3);
        g.setColor(estaLento() ? Color.GRAY : nave.getColor());
        g.fillPolygon(pol);
        g.setColor(Color.WHITE);
        g.drawPolygon(pol);

        // HUD
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.setColor(Color.WHITE);
        g.drawString("Piloto: " + piloto.getNombre() + "  |  Nave: " + nave, 10, 20);
        g.drawString("Puntaje: " + puntaje, ANCHO - 140, 20);
        if (estaLento()) {
            g.setColor(Color.ORANGE);
            g.drawString("¡RALENTIZADO!", ANCHO / 2 - 60, 20);
        }
    }

    private void dibujarObjeto(Graphics2D g, ObjetoEspacial o) {
        int x = o.getX(), y = o.getY(), w = o.getAncho(), h = o.getAlto();
        switch (o.getTipo()) {
            case ENEMIGO:
                g.setColor(new Color(220, 40, 40));
                g.fillPolygon(new int[]{x + w, x, x + w}, new int[]{y, y + h / 2, y + h}, 3);
                break;
            case ASTEROIDE:
                g.setColor(new Color(130, 110, 90));
                g.fillOval(x, y, w, h);
                g.setColor(new Color(90, 75, 60));
                g.fillOval(x + 10, y + 10, 10, 10);
                break;
            case CAPSULA:
                g.setColor(new Color(60, 220, 90));
                g.fillRoundRect(x, y, w, h, 6, 6);
                g.setColor(Color.WHITE);
                g.drawString("+", x + 6, y + 15);
                break;
            case NUCLEO:
                g.setColor(Color.CYAN);
                g.fillOval(x, y, w, h);
                g.setColor(Color.WHITE);
                g.drawOval(x + 4, y + 4, w - 8, h - 8);
                break;
        }
    }

    // ------------------------------------------------------------ teclado
    @Override
    public void keyPressed(KeyEvent e) { cambiarTecla(e.getKeyCode(), true); }

    @Override
    public void keyReleased(KeyEvent e) { cambiarTecla(e.getKeyCode(), false); }

    @Override
    public void keyTyped(KeyEvent e) { }

    private void cambiarTecla(int k, boolean valor) {
        if (k == KeyEvent.VK_UP || k == KeyEvent.VK_W) arriba = valor;
        else if (k == KeyEvent.VK_DOWN || k == KeyEvent.VK_S) abajo = valor;
        else if (k == KeyEvent.VK_LEFT || k == KeyEvent.VK_A) izq = valor;
        else if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_D) der = valor;
        else if (k == KeyEvent.VK_SPACE) disparando = valor;
    }
}
