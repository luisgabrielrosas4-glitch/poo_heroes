public class TestRetoSecuencia {
    static void revisar(boolean condicion) {
        if (!condicion) throw new AssertionError("Prueba fallida");
    }

    static void probarNivel(NivelDificultad nivel, String secuencia, String respuesta) {
        RetoSecuencia reto = new RetoSecuencia(nivel);
        revisar(reto.mostrarSecuencia().equals(secuencia));
        revisar(reto.evaluarRespuesta(respuesta));
        revisar(!reto.evaluarRespuesta("999"));
        revisar(!reto.evaluarRespuesta(""));
        revisar(!reto.evaluarRespuesta("hola"));
        revisar(!reto.evaluarRespuesta("10.0"));
        revisar(!reto.evaluarRespuesta(null));
        reto.generarProblema();
        revisar(reto.mostrarSecuencia().equals(secuencia));
        revisar(reto.evaluarRespuesta(respuesta));
    }

    static void probarJoker() {
        Joker joker = new Joker();
        RetoSecuencia facil = new RetoSecuencia(NivelDificultad.FACIL);
        Mision misionMedia = joker.plantearMision("media", facil);
        revisar(facil.getDificultad() == NivelDificultad.MEDIO);
        revisar(misionMedia.getReto() == facil);
        revisar(facil.mostrarSecuencia().equals("3, 6, 12, 24, ?"));
        revisar(facil.evaluarRespuesta("48"));
        revisar(!facil.evaluarRespuesta("10"));
        revisar(!misionMedia.procesarIntento("10"));
        revisar(misionMedia.consultarIntentosRestantes() == 2);
        revisar(misionMedia.procesarIntento("48"));
        revisar("completada".equals(misionMedia.getEstado()));

        RetoSecuencia medio = new RetoSecuencia(NivelDificultad.MEDIO);
        Mision misionDificil = joker.plantearMision("dificil", medio);
        revisar(medio.getDificultad() == NivelDificultad.DIFICIL);
        revisar(medio.mostrarSecuencia().equals("2, 3, 5, 8, ?"));
        revisar(medio.evaluarRespuesta("13"));
        revisar(!medio.evaluarRespuesta("48"));
        revisar(!misionDificil.procesarIntento("48"));
        revisar(misionDificil.procesarIntento("13"));
        revisar("completada".equals(misionDificil.getEstado()));
    }

    public static void main(String[] args) {
        probarNivel(NivelDificultad.FACIL, "2, 4, 6, 8, ?", "10");
        probarNivel(NivelDificultad.MEDIO, "3, 6, 12, 24, ?", "48");
        probarNivel(NivelDificultad.DIFICIL, "2, 3, 5, 8, ?", "13");

        RetoSecuencia reto = new RetoSecuencia(NivelDificultad.FACIL);
        reto.aumentarDificultad();
        revisar(reto.mostrarSecuencia().equals("3, 6, 12, 24, ?"));
        revisar(reto.evaluarRespuesta("48"));
        revisar(!reto.evaluarRespuesta("10"));
        probarJoker();
        System.out.println("Pruebas correctas");
    }
}
