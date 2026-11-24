import java.util.ArrayList;
import java.util.List;

public class Partida {
    private List<Mision> misiones;
    private int recompensasGlobales;

    public Partida() {
        this.misiones = new ArrayList<>();
        this.recompensasGlobales = 0;
    }

    public void agregarMision(Mision m) {
        if (m == null || m.getPartida() != null) return;
        m.asignarPartida(this);
        misiones.add(m);
    }

    public boolean acreditarRecompensa(Heroe heroe, Mision mision) {
        if (heroe == null || mision == null || mision.getPartida() != this
                || !misiones.contains(mision)) return false;
        if (!mision.marcarRecompensaAcreditada()) return false;
        heroe.recibirRecompensa(mision.getRecompensa());
        recompensasGlobales += mision.getRecompensa();
        return true;
    }

    public int getRecompensasGlobales() { return recompensasGlobales; }

    public void iniciarJuego() {
        // TODO: Implementar inicio del juego
    }
}
