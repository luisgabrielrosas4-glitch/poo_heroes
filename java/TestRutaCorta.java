import java.util.Map;

public class TestRutaCorta {
    static void revisar(boolean valor) {
        if (!valor) throw new AssertionError("Prueba fallida");
    }

    public static void main(String[] args) {
        RetoRutaCorta reto = new RetoRutaCorta();
        revisar(reto.costoMinimo() == 5);
        revisar(reto.evaluarRespuesta("A-B-C-D"));
        revisar(!reto.evaluarRespuesta("A-B-D"));
        revisar(!reto.evaluarRespuesta("A-D"));
        revisar(!reto.evaluarRespuesta("A-X-D"));

        GreenGoblin goblin = new GreenGoblin();
        revisar(new Mision("1", reto).aplicarDificultad(goblin));
        revisar(reto.costoMinimo() == 6);
        revisar(reto.evaluarRespuesta("A-B-D"));

        RetoSecuencia otro = new RetoSecuencia(NivelDificultad.FACIL);
        revisar(!new Mision("2", otro).aplicarDificultad(goblin));
        revisar(otro.getDificultad() == NivelDificultad.FACIL);

        RetoRutaCorta r2 = new RetoRutaCorta();
        Mision terminada = new Mision("3", r2);
        terminada.completar();
        revisar(!terminada.aplicarDificultad(goblin));
        revisar(r2.costoMinimo() == 5);

        RetoRutaCorta r3 = new RetoRutaCorta();
        Mision bloqueada = new Mision("4", r3);
        for (int i = 0; i < 3; i++) bloqueada.procesarIntento("mal");
        revisar(!bloqueada.aplicarDificultad(goblin));
        revisar(r3.costoMinimo() == 5);

        RetoRutaCorta sinSalida = new RetoRutaCorta("A", "B");
        sinSalida.agregarTramo("A", "B", 3);
        revisar(!new Mision("5", sinSalida).aplicarDificultad(new GreenGoblin("A", "B")));
        revisar(sinSalida.evaluarRespuesta("A-B"));

        RetoRutaCorta creado = new RetoRutaCorta();
        new GreenGoblin().plantearMision("6", creado);
        revisar(creado.evaluarRespuesta("A-B-D"));

        pruebasContextoMision();
        System.out.println("Pruebas correctas");
    }

    static boolean lanzaEstadoIlegal(Runnable accion) {
        try {
            accion.run();
            return false;
        } catch (IllegalStateException e) {
            return true;
        }
    }

    /** Verifica que el reto conserve grafo, costo y solución tras intentar el obstáculo. */
    static void revisarIntacto(RetoRutaCorta reto, Map<String, Map<String, Integer>> grafoInicial) {
        revisar(reto.copiarGrafo().equals(grafoInicial));
        revisar(reto.costoMinimo() == 5);
        revisar(reto.evaluarRespuesta("A-B-C-D"));
        revisar(!reto.evaluarRespuesta("A-B-D"));
    }

    static void pruebasContextoMision() {
        GreenGoblin goblin = new GreenGoblin();

        // Caso de aceptación: completar con A-B-C-D (costo 5) y luego aplicar el obstáculo.
        RetoRutaCorta reto = new RetoRutaCorta();
        Mision mision = new Mision("c1", reto);
        Map<String, Map<String, Integer>> inicial = reto.copiarGrafo();
        revisar(mision.procesarIntento("A-B-C-D"));
        revisar("completada".equals(mision.consultarEstado()));
        revisar(reto.costoMinimo() == 5);

        // 1) Llamada mediante Mision.
        revisar(!mision.aplicarDificultad(goblin));
        revisarIntacto(reto, inicial);
        // 2) Acceso directo que antes evitaba la validación.
        goblin.complicarReto(reto);
        revisarIntacto(reto, inicial);
        revisar(!goblin.aplicar(reto));
        revisarIntacto(reto, inicial);
        revisar(!reto.bloquearTramo("B", "C"));
        revisarIntacto(reto, inicial);
        revisar("completada".equals(mision.consultarEstado()));

        // Las demás operaciones que modifican el reto también se rechazan.
        revisar(lanzaEstadoIlegal(() -> reto.agregarTramo("A", "D", 1)));
        revisar(lanzaEstadoIlegal(reto::generarProblema));
        revisar(lanzaEstadoIlegal(reto::aumentarDificultad));
        revisar(lanzaEstadoIlegal(() -> reto.setDificultad(NivelDificultad.DIFICIL)));
        revisar(reto.getDificultad() == NivelDificultad.FACIL);
        revisarIntacto(reto, inicial);

        // Misión bloqueada: mismo comportamiento, mediante Mision y de forma directa.
        RetoRutaCorta r2 = new RetoRutaCorta();
        Mision bloqueada = new Mision("c2", r2);
        Map<String, Map<String, Integer>> inicial2 = r2.copiarGrafo();
        for (int i = 0; i < 3; i++) bloqueada.procesarIntento("mal");
        revisar("bloqueada".equals(bloqueada.consultarEstado()));
        revisar(!bloqueada.aplicarDificultad(goblin));
        goblin.complicarReto(r2);
        revisar(!goblin.aplicar(r2));
        revisarIntacto(r2, inicial2);

        // Misión activa: el obstáculo directo sí se aplica, y cambia al cerrarse la misión.
        RetoRutaCorta r3 = new RetoRutaCorta();
        Mision activa = new Mision("c3", r3);
        revisar(goblin.aplicar(r3));
        revisar(r3.costoMinimo() == 6);
        activa.completar();
        Map<String, Map<String, Integer>> tras = r3.copiarGrafo();
        revisar(!new GreenGoblin("A", "B").aplicar(r3));
        revisar(r3.copiarGrafo().equals(tras) && r3.costoMinimo() == 6);

        // Regla anterior: un obstáculo que deja el destino inaccesible se rechaza aunque la misión esté activa.
        RetoRutaCorta unico = new RetoRutaCorta("A", "B");
        unico.agregarTramo("A", "B", 3);
        new Mision("c4", unico);
        Map<String, Map<String, Integer>> grafoUnico = unico.copiarGrafo();
        revisar(!new GreenGoblin("A", "B").aplicar(unico));
        revisar(unico.copiarGrafo().equals(grafoUnico) && unico.evaluarRespuesta("A-B"));

        // Un reto sin misión (en preparación) se puede modificar; una misión por reto.
        RetoRutaCorta libre = new RetoRutaCorta();
        revisar(libre.admiteModificaciones() && goblin.aplicar(libre));
        revisar(lanzaEstadoIlegal(() -> new Mision("c5", reto)));
        revisarIntacto(reto, inicial);

        // Joker respeta el estado de la misión igual que Green Goblin.
        RetoSecuencia secuencia = new RetoSecuencia(NivelDificultad.FACIL);
        Mision cerrada = new Mision("c6", secuencia);
        cerrada.completar();
        new Joker().complicarReto(secuencia);
        revisar(secuencia.getDificultad() == NivelDificultad.FACIL);
    }
}
