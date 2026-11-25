package es.juanjovera.chuletas.jardin.riego;

import java.util.Random;

/**
 * Jardín con riego: 12 visitantes, como mucho 5 dentro, y un jardinero que riega 3 veces con el jardín vacío
 * y sin dejar entrar a nadie mientras riega.
 */
public class Main {
    private static final int VISITANTES = 12;

    public static void main(String[] args) throws InterruptedException {
        Jardin jardin = new Jardin();          // UN solo objeto compartido por todos los hilos
        Random azar = new Random();

        Thread jardinero = new Thread(new Jardinero(jardin));
        jardinero.start();

        Thread[] hilos = new Thread[VISITANTES];
        for (int i = 0; i < hilos.length; i++) {
            hilos[i] = new Thread(new Visitante(i + 1, jardin));
            hilos[i].start();
            Thread.sleep(azar.nextInt(200, 601)); // llega uno cada 200-600 ms
        }
        for (Thread hilo : hilos) {
            hilo.join();
        }
        jardinero.join();                      // el jardinero termina solo tras sus 3 riegos
        System.out.println("Fin: han visitado el jardín " + VISITANTES + " personas.");
    }
}
