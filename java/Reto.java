public abstract class Reto {
    protected String descripcion;
    protected NivelDificultad dificultad;
    private Mision mision;

    public Reto(String descripcion, NivelDificultad dificultad) {
        this.descripcion = descripcion;
        this.dificultad = dificultad;
    }

    public abstract boolean evaluarRespuesta(String respuesta);
    public abstract void generarProblema();

    /**
     * Contexto de la misión: la propia Mision se registra aquí desde su constructor.
     * Un reto pertenece a una sola misión; asociarlo a otra distinta lanza
     * IllegalStateException para que una misión cerrada no pueda "liberar" su reto.
     */
    void asociarMision(Mision nueva) {
        if (mision != null && mision != nueva) {
            throw new IllegalStateException("El reto ya pertenece a otra misión");
        }
        mision = nueva;
    }

    /**
     * true si el reto no tiene misión (aún se está preparando) o si su misión
     * sigue activa. El estado se consulta en vivo, sin copiarlo.
     */
    public boolean admiteModificaciones() {
        return mision == null || mision.estaActiva();
    }

    /** Las operaciones que modifican el reto llaman esto antes de tocar cualquier dato. */
    protected void exigirModificable() {
        if (!admiteModificaciones()) {
            throw new IllegalStateException("La misión del reto no está activa");
        }
    }

    public void aumentarDificultad() {
        exigirModificable();
        if (this.dificultad == NivelDificultad.FACIL) {
            this.dificultad = NivelDificultad.MEDIO;
        } else if (this.dificultad == NivelDificultad.MEDIO) {
            this.dificultad = NivelDificultad.DIFICIL;
        }
    }

    public NivelDificultad getDificultad() {
        return dificultad;
    }

    public void setDificultad(NivelDificultad dificultad) {
        exigirModificable();
        this.dificultad = dificultad;
    }
}
