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
        revisar(misionMedia.getReto() != facil);
        revisar(((RetoSecuencia) misionMedia.getReto()).mostrarSecuencia().equals("3, 6, 12, 24, ?"));
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

    static void probarCambiosDirectosYReutilizacion() {
        RetoSecuencia reto = new RetoSecuencia(NivelDificultad.FACIL);
        Mision primera = new Mision("original", reto);
        reto.setDificultad(NivelDificultad.MEDIO);
        revisar(reto.mostrarSecuencia().equals("3, 6, 12, 24, ?"));
        revisar(reto.evaluarRespuesta("48") && !reto.evaluarRespuesta("10"));
        revisar(((RetoSecuencia) primera.getReto()).mostrarSecuencia().equals("2, 4, 6, 8, ?"));
        revisar(primera.procesarIntento("10"));
        Mision segunda = new Mision("segunda", reto);
        reto.setDificultad(NivelDificultad.DIFICIL);
        revisar(reto.mostrarSecuencia().equals("2, 3, 5, 8, ?"));
        revisar(((RetoSecuencia) segunda.getReto()).mostrarSecuencia().equals("3, 6, 12, 24, ?"));
        revisar(segunda.procesarIntento("48"));
        boolean invalido = false;
        try { reto.setDificultad(null); }
        catch (IllegalArgumentException e) { invalido = true; }
        revisar(invalido && reto.getDificultad() == NivelDificultad.DIFICIL);

        Joker joker = new Joker();
        RetoSecuencia compartido = new RetoSecuencia(NivelDificultad.FACIL);
        Mision m1 = joker.plantearMision("uno", compartido);
        Mision m2 = joker.plantearMision("dos", compartido);
        revisar(m1.getReto() != m2.getReto());
        revisar(((RetoSecuencia) m1.getReto()).mostrarSecuencia().equals("3, 6, 12, 24, ?"));
        revisar(((RetoSecuencia) m2.getReto()).mostrarSecuencia().equals("2, 3, 5, 8, ?"));
        revisar(m1.procesarIntento("48"));
        revisar(m2.procesarIntento("13"));

        RetoSecuencia formato = new RetoSecuencia(NivelDificultad.FACIL);
        revisar(formato.evaluarRespuesta("\t10\n"));
        revisar(formato.evaluarRespuesta("\u000010\u0020"));
        revisar(!formato.evaluarRespuesta("\u00A010"));
        revisar(!formato.evaluarRespuesta("10\u00A0"));
        revisar(!formato.evaluarRespuesta("2147483648"));
        revisar(formato.evaluarRespuesta("0000000000000000010"));
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
        probarCambiosDirectosYReutilizacion();
        System.out.println("Pruebas correctas");
    }
}
