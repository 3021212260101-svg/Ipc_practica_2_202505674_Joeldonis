package quetzal;

import java.awt.Color;

/**
 * Modelos de nave. Cada modelo define la dificultad:
 * - sleepMovimiento: ms de sleep() del hilo de movimiento (menor = más rápida).
 * - cooldownDisparo: ms de sleep() del hilo de disparo (menor = más cadencia).
 */
public enum TipoNave {
    EXPLORADOR("Explorador", "Fácil", 6, 2000, new Color(80, 200, 120)),
    CAZA_ESTELAR("Caza Estelar", "Normal", 12, 1000, new Color(80, 160, 255)),
    ACORAZADO("Acorazado", "Difícil", 22, 300, new Color(255, 150, 60));

    private final String nombre;
    private final String dificultad;
    private final int sleepMovimiento;
    private final int cooldownDisparo;
    private final Color color;

    TipoNave(String nombre, String dificultad, int sleepMovimiento, int cooldownDisparo, Color color) {
        this.nombre = nombre;
        this.dificultad = dificultad;
        this.sleepMovimiento = sleepMovimiento;
        this.cooldownDisparo = cooldownDisparo;
        this.color = color;
    }

    public String getNombre() { return nombre; }
    public String getDificultad() { return dificultad; }
    public int getSleepMovimiento() { return sleepMovimiento; }
    public int getCooldownDisparo() { return cooldownDisparo; }
    public Color getColor() { return color; }

    @Override
    public String toString() {
        return nombre + " (" + dificultad + ")";
    }
}
