package es.juanjovera.chuletas.barbero.semaforo;

import java.util.Random;

/**
 * Barbero dormido: 1 barbero, 1 silla de corte y una sala de espera con 4 sillas. Llegan 10 clientes.
 *
 * Cómo termina un hilo que tiene un bucle infinito (el barbero):
 * 1. el main espera (join) a que terminen todos los clientes; cada cliente solo termina cuando lo han pelado
 *    o se ha marchado, así que en ese momento el barbero ya no tiene trabajo;
 * 2. el main interrumpe al barbero: está dormido esperando clientes y le salta InterruptedException;
 * 3. el main espera (join) también al barbero.
 */
public class Main {
    private static final int SILLAS_ESPERA = 4;
    private static final int CLIENTES = 10;

    public static void main(String[] args) throws InterruptedException {
        Barberia barberia = new Barberia(SILLAS_ESPERA); // UN solo objeto compartido
        Random azar = new Random();

        Thread barbero = new Thread(new Barbero(barberia));
        barbero.start();

        Thread[] clientes = new Thread[CLIENTES];
        for (int i = 0; i < clientes.length; i++) {
            Thread.sleep(azar.nextInt(300, 1201)); // primero la pausa: así el barbero ya duerme cuando llega el 1
            clientes[i] = new Thread(new Cliente(i + 1, barberia));
            clientes[i].start();
        }
        for (Thread cliente : clientes) {
            cliente.join();
        }
        barbero.interrupt(); // única forma limpia de sacarlo de su while (true)
        barbero.join();
        System.out.println("Fin: la barbería cierra.");
    }
}
