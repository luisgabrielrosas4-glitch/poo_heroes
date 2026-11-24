public class TestRecompensaMision {
    static class RetoPrueba extends Reto {
        RetoPrueba() { super("Prueba", NivelDificultad.FACIL); }
        public boolean evaluarRespuesta(String respuesta) { return "bien".equals(respuesta); }
        public void generarProblema() { }
    }

    static void comprobar(boolean ok, String caso) {
        if (!ok) throw new AssertionError(caso);
    }

    public static void main(String[] args) {
        Partida partida = new Partida();
        Batman batman = new Batman();
        Batman otro = new Batman();
        Mision primera = new Mision("1", new RetoPrueba(), 30);
        partida.agregarMision(primera);
        comprobar(!partida.acreditarRecompensa(batman, primera), "activa");
        comprobar(primera.procesarIntento("bien"), "completar");
        comprobar(partida.acreditarRecompensa(batman, primera), "primera acreditacion");
        comprobar(batman.consultarSaldo() == 130, "saldo correcto");
        comprobar(partida.getRecompensasGlobales() == 30, "total correcto");
        comprobar(primera.isRecompensaAcreditada(), "marca de pago");
        comprobar(!partida.acreditarRecompensa(batman, primera), "no repetir");
        comprobar(!partida.acreditarRecompensa(otro, primera), "otro heroe");
        comprobar(otro.consultarSaldo() == 100, "sin dinero para otro");
        comprobar(!batman.acreditarRecompensa(primera), "pago directo repetido");
        comprobar(batman.consultarSaldo() == 130 && partida.getRecompensasGlobales() == 30, "sin duplicados");

        Mision segunda = new Mision("2", new RetoPrueba(), 50);
        partida.agregarMision(segunda);
        comprobar(segunda.procesarIntento("bien"), "segunda completada");
        comprobar(partida.acreditarRecompensa(batman, segunda), "segunda acreditada");
        comprobar(batman.consultarSaldo() == 180 && partida.getRecompensasGlobales() == 80, "dos misiones");

        Mision bloqueada = new Mision("3", new RetoPrueba(), 20);
        partida.agregarMision(bloqueada);
        for (int i = 0; i < 3; i++) bloqueada.procesarIntento("mal");
        comprobar(!partida.acreditarRecompensa(batman, bloqueada), "bloqueada");
        comprobar(batman.consultarSaldo() == 180 && partida.getRecompensasGlobales() == 80, "total estable");

        Mision ajena = new Mision("4", new RetoPrueba(), 10);
        ajena.completar();
        comprobar(!partida.acreditarRecompensa(batman, ajena), "mision ajena");
        comprobar(!ajena.isRecompensaAcreditada(), "ajena sin cobrar");
        comprobar(new Mision("5", new RetoPrueba()).getRecompensa() == 10, "recompensa predeterminada");
        boolean invalida = false;
        try { new Mision("6", new RetoPrueba(), 0); }
        catch (IllegalArgumentException e) { invalida = true; }
        comprobar(invalida, "recompensa cero");
        // Primero cobra directamente el heroe y luego se intenta por Partida.
        Partida pDirecta = new Partida();
        Batman hDirecto = new Batman();
        Batman hDistinto = new Batman();
        Mision mDirecta = new Mision("directa", new RetoPrueba(), 30);
        pDirecta.agregarMision(mDirecta);
        mDirecta.completar();
        comprobar(hDirecto.acreditarRecompensa(mDirecta), "cobro directo");
        comprobar(hDirecto.consultarSaldo() == 130, "saldo del cobro directo");
        comprobar(pDirecta.getRecompensasGlobales() == 30, "total del cobro directo");
        comprobar(!pDirecta.acreditarRecompensa(hDirecto, mDirecta), "directo y luego partida");
        comprobar(!hDirecto.acreditarRecompensa(mDirecta), "repetir directo");
        comprobar(!hDistinto.acreditarRecompensa(mDirecta), "otro heroe directo");
        comprobar(!pDirecta.acreditarRecompensa(hDistinto, mDirecta), "otro heroe partida");
        comprobar(hDistinto.consultarSaldo() == 100, "otro heroe sin premio");
        comprobar(hDirecto.consultarSaldo() == 130 && pDirecta.getRecompensasGlobales() == 30, "sin cobro doble");

        // Primero se cobra con Partida y después directamente.
        Partida pInversa = new Partida();
        Batman hInverso = new Batman();
        Mision mInversa = new Mision("inversa", new RetoPrueba(), 30);
        pInversa.agregarMision(mInversa);
        mInversa.completar();
        comprobar(pInversa.acreditarRecompensa(hInverso, mInversa), "primero partida");
        comprobar(!hInverso.acreditarRecompensa(mInversa), "despues directo");
        comprobar(!pInversa.acreditarRecompensa(hInverso, mInversa), "repetir partida");
        comprobar(hInverso.consultarSaldo() == 130 && pInversa.getRecompensasGlobales() == 30, "totales inversos");

        Mision noRegistrada = new Mision("externa", new RetoPrueba(), 20);
        noRegistrada.completar();
        comprobar(!hDirecto.acreditarRecompensa(noRegistrada), "sin partida no paga");
        Partida otraPartida = new Partida();
        otraPartida.agregarMision(mDirecta);
        comprobar(!otraPartida.acreditarRecompensa(hDirecto, mDirecta), "no mover mision a otra partida");
        comprobar(otraPartida.getRecompensasGlobales() == 0, "otra partida sin premio");
        comprobar(!hDirecto.acreditarRecompensa(bloqueada), "bloqueada directa");
        comprobar(batman.consultarSaldo() == 180 && partida.getRecompensasGlobales() == 80, "totales originales");
        System.out.println("Pruebas de recompensas correctas");
    }
}
