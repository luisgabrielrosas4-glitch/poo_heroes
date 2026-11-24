import java.util.ArrayList;
import java.util.List;

public class Partida {
    private List<Mision> misiones;
    private int recompensasGlobales;

    public Partida() {
        this.misiones = new ArrayList<>();
        this.recompensasGlobales = 0;
    }

    public synchronized void agregarMision(Mision m) {
        if (m != null && m.asignarPartida(this)) misiones.add(m);
    }

    public synchronized boolean acreditarRecompensa(Heroe heroe, Mision mision) {
        if (heroe == null || mision == null || mision.getPartida() != this
                || !misiones.contains(mision)) return false;

        synchronized (mision) {
            if (!"completada".equals(mision.getEstado())
                    || mision.isRecompensaAcreditada()) return false;
            int cantidad = mision.getRecompensa();
            if (cantidad <= 0 || recompensasGlobales > Integer.MAX_VALUE - cantidad) return false;
            if (!heroe.recibirRecompensa(cantidad)) return false;
            mision.marcarRecompensaAcreditada();
            recompensasGlobales += cantidad;
            return true;
        }
    }

    public synchronized int getRecompensasGlobales() { return recompensasGlobales; }

    public void iniciarJuego() {
        // TODO: Implementar inicio del juego
    }
}
