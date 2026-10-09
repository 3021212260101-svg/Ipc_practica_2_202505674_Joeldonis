package quetzal;

/** Registro de una partida finalizada (se guarda en el historial). */
public class Partida {
    private final String piloto;
    private final TipoNave nave;
    private final int puntaje;
    private final int enemigosDestruidos;
    private final String fecha;

    public Partida(String piloto, TipoNave nave, int puntaje, int enemigosDestruidos, String fecha) {
        this.piloto = piloto;
        this.nave = nave;
        this.puntaje = puntaje;
        this.enemigosDestruidos = enemigosDestruidos;
        this.fecha = fecha;
    }

    public String getPiloto() { return piloto; }
    public TipoNave getNave() { return nave; }
    public int getPuntaje() { return puntaje; }
    public int getEnemigosDestruidos() { return enemigosDestruidos; }
    public String getFecha() { return fecha; }
}
