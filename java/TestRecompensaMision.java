import java.util.concurrent.atomic.AtomicInteger;

public class TestRecompensaMision {
    static class RetoPrueba extends Reto {
        RetoPrueba() { super("Prueba", NivelDificultad.FACIL); }
        public boolean evaluarRespuesta(String respuesta) { return "bien".equals(respuesta); }
        public void generarProblema() { }
    }

    static void comprobar(boolean ok, String caso) {
        if (!ok) throw new AssertionError(caso);
    }

    static void pruebaConcurrencia() throws InterruptedException {
        Partida partida = new Partida();
        Batman heroe = new Batman();
        Mision[] misiones = new Mision[800];
        for (int i = 0; i < misiones.length; i++) {
            misiones[i] = new Mision("paralela" + i, new RetoPrueba(), 3);
            partida.agregarMision(misiones[i]);
            misiones[i].completar();
        }
        AtomicInteger aciertos = new AtomicInteger();
        Thread[] hilos = new Thread[8];
        for (int hilo = 0; hilo < hilos.length; hilo++) {
            final int inicio = hilo;
            hilos[hilo] = new Thread(() -> {
                for (int i = inicio; i < misiones.length; i += 8) {
                    boolean cobro = (i % 2 == 0)
                            ? heroe.acreditarRecompensa(misiones[i])
                            : partida.acreditarRecompensa(heroe, misiones[i]);
                    if (cobro) aciertos.incrementAndGet();
                }
            });
            hilos[hilo].start();
        }
        for (Thread hilo : hilos) hilo.join();
        comprobar(aciertos.get() == 800, "todos los cobros simultaneos");
        comprobar(heroe.consultarSaldo() == 2500, "saldo simultaneo");
        comprobar(partida.getRecompensasGlobales() == 2400, "total simultaneo");

        Batman compartido = new Batman();
        Partida[] partidas = new Partida[400];
        Mision[] distintas = new Mision[400];
        for (int i = 0; i < distintas.length; i++) {
            partidas[i] = new Partida();
            distintas[i] = new Mision("distinta" + i, new RetoPrueba(), 2);
            partidas[i].agregarMision(distintas[i]);
            distintas[i].completar();
        }
        Thread[] otros = new Thread[8];
        for (int hilo = 0; hilo < 8; hilo++) {
            final int inicio = hilo;
            otros[hilo] = new Thread(() -> {
                for (int i = inicio; i < distintas.length; i += 8) {
                    if (!compartido.acreditarRecompensa(distintas[i]))
                        throw new AssertionError("cobro de partida distinta");
                }
            });
            otros[hilo].start();
        }
        for (Thread hilo : otros) hilo.join();
        comprobar(compartido.consultarSaldo() == 900, "saldo compartido entre partidas");
        int total = 0;
        for (Partida p : partidas) total += p.getRecompensasGlobales();
        comprobar(total == 800, "suma de partidas");
    }

    static void pruebaDesbordamiento() {
        Partida p = new Partida();
        Heroe lleno = new Heroe("Lleno", Integer.MAX_VALUE, 3) {};
        Mision m = new Mision("lleno", new RetoPrueba(), 30);
        p.agregarMision(m);
        m.completar();
        comprobar(!p.acreditarRecompensa(lleno, m), "saldo no desborda");
        comprobar(lleno.consultarSaldo() == Integer.MAX_VALUE, "saldo intacto");
        comprobar(!m.isRecompensaAcreditada() && p.getRecompensasGlobales() == 0,
                "mision no pagada ante error de saldo");
        Batman normal = new Batman();
        comprobar(normal.acreditarRecompensa(m), "se permite reintentar");

        Partida otra = new Partida();
        Heroe desdeCero = new Heroe("Cero", 0, 3) {};
        Mision grande = new Mision("grande", new RetoPrueba(), Integer.MAX_VALUE);
        otra.agregarMision(grande);
        grande.completar();
        comprobar(desdeCero.acreditarRecompensa(grande), "monto maximo permitido");
        Mision extra = new Mision("extra", new RetoPrueba(), 1);
        otra.agregarMision(extra);
        extra.completar();
        Batman batman = new Batman();
        comprobar(!otra.acreditarRecompensa(batman, extra), "total global no desborda");
        comprobar(!extra.isRecompensaAcreditada(), "extra sin marcar");
        comprobar(batman.consultarSaldo() == 100, "batman no recibe extra");
        comprobar(otra.getRecompensasGlobales() == Integer.MAX_VALUE, "total maximo intacto");
    }

    public static void main(String[] args) throws InterruptedException {
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
        pruebaDesbordamiento();
        pruebaConcurrencia();
        System.out.println("Pruebas de recompensas correctas");
    }
}
