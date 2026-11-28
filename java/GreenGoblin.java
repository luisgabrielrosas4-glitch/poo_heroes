public class GreenGoblin extends Villano implements Dificultad {
    private String a;
    private String b;

    public GreenGoblin() {
        this("B", "C");
    }

    public GreenGoblin(String a, String b) {
        super("Green Goblin", "RetoRutaCorta");
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean aplicar(Reto reto) {
        if (!(reto instanceof RetoRutaCorta)) return false;
        return ((RetoRutaCorta) reto).bloquearTramo(a, b);
    }

    /** Sin efecto si la misión del reto no está activa (la firma void no cambia). */
    @Override
    public void complicarReto(Reto reto) {
        aplicar(reto);
    }

    @Override
    public Mision plantearMision(String id, Reto reto) {
        Mision mision = new Mision(id, reto);
        mision.aplicarDificultad(this);
        return mision;
    }
}
