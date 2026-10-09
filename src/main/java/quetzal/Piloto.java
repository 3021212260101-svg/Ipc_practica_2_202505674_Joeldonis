package quetzal;

/** Piloto registrado en el sistema con su nave favorita. */
public class Piloto {
    private final String nombre;
    private TipoNave nave;

    public Piloto(String nombre, TipoNave nave) {
        this.nombre = nombre;
        this.nave = nave;
    }

    public String getNombre() { return nombre; }
    public TipoNave getNave() { return nave; }
    public void setNave(TipoNave nave) { this.nave = nave; }

    @Override
    public String toString() { return nombre; }
}
