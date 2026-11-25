package es.juanjovera.chuletas.jardin.sync;

import java.util.Random;

/**
 * Jardín con aforo: 12 visitantes, como mucho 5 dentro a la vez.
 */
public class Main {
    private static final int VISITANTES = 12;

    public static void main(String[] args) throws InterruptedException {
        Jardin jardin = new Jardin();          // UN solo objeto compartido por todos los hilos
        Random azar = new Random();
        Thread[] hilos = new Thread[VISITANTES];

        for (int i = 0; i < hilos.length; i++) {
            hilos[i] = new Thread(new Visitante(i + 1, jardin));
            hilos[i].start();
            Thread.sleep(azar.nextInt(200, 601)); // llega uno cada 200-600 ms
        }
        for (Thread hilo : hilos) {
            hilo.join();                       // el main espera a que salgan TODOS
        }
        System.out.println("Fin: han visitado el jardín " + VISITANTES + " personas.");
    }
}
