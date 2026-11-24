public abstract class Heroe extends Personaje {
    protected int saldo;
    protected int intentosRestantes;

    public Heroe(String nombre, int saldo, int intentosRestantes) {
        super(nombre);
        this.saldo = saldo;
        this.intentosRestantes = intentosRestantes;
    }

    public int consultarSaldo() {
        return saldo;
    }

    public boolean pagar(int costo) {
        if (saldo >= costo) {
            saldo -= costo;
            return true;
        }
        return false;
    }

    public boolean acreditarRecompensa(Mision mision) {
        if (mision == null || mision.getPartida() == null) return false;
        return mision.getPartida().acreditarRecompensa(this, mision);
    }

    void recibirRecompensa(int cantidad) {
        saldo += cantidad;
    }

    public void restarIntento() {
        if (intentosRestantes > 0) {
            intentosRestantes--;
        }
    }
}
