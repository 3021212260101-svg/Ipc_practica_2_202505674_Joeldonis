package quetzal;

/**
 * Persistencia en memoria usando exclusivamente vectores (arreglos).
 * Los arreglos crecen manualmente cuando se llenan (sin ArrayList ni colecciones).
 */
public class GestorDatos {
    private static Piloto[] pilotos = new Piloto[10];
    private static int numPilotos = 0;
    private static Partida[] partidas = new Partida[20];
    private static int numPartidas = 0;

    private GestorDatos() { }

    /** Registra un piloto. Devuelve false si el nombre ya existe. */
    public static synchronized boolean agregarPiloto(String nombre, TipoNave nave) {
        if (buscarPiloto(nombre) != null) {
            return false;
        }
        if (numPilotos == pilotos.length) {
            Piloto[] nuevo = new Piloto[pilotos.length * 2];
            for (int i = 0; i < numPilotos; i++) nuevo[i] = pilotos[i];
            pilotos = nuevo;
        }
        pilotos[numPilotos++] = new Piloto(nombre, nave);
        return true;
    }

    public static synchronized Piloto buscarPiloto(String nombre) {
        for (int i = 0; i < numPilotos; i++) {
            if (pilotos[i].getNombre().equalsIgnoreCase(nombre)) return pilotos[i];
        }
        return null;
    }

    /** Devuelve una copia del vector de pilotos con el tamaño exacto. */
    public static synchronized Piloto[] getPilotos() {
        Piloto[] copia = new Piloto[numPilotos];
        for (int i = 0; i < numPilotos; i++) copia[i] = pilotos[i];
        return copia;
    }

    public static synchronized void agregarPartida(Partida p) {
        if (numPartidas == partidas.length) {
            Partida[] nuevo = new Partida[partidas.length * 2];
            for (int i = 0; i < numPartidas; i++) nuevo[i] = partidas[i];
            partidas = nuevo;
        }
        partidas[numPartidas++] = p;
    }

    /** Historial completo en orden cronológico. */
    public static synchronized Partida[] getPartidas() {
        Partida[] copia = new Partida[numPartidas];
        for (int i = 0; i < numPartidas; i++) copia[i] = partidas[i];
        return copia;
    }

    /** Devuelve las mejores 'n' partidas ordenadas de mayor a menor puntaje (burbuja). */
    public static synchronized Partida[] getTop(int n) {
        Partida[] orden = getPartidas();
        for (int i = 0; i < orden.length - 1; i++) {
            for (int j = 0; j < orden.length - 1 - i; j++) {
                if (orden[j].getPuntaje() < orden[j + 1].getPuntaje()) {
                    Partida tmp = orden[j];
                    orden[j] = orden[j + 1];
                    orden[j + 1] = tmp;
                }
            }
        }
        int tam = Math.min(n, orden.length);
        Partida[] top = new Partida[tam];
        for (int i = 0; i < tam; i++) top[i] = orden[i];
        return top;
    }
}
