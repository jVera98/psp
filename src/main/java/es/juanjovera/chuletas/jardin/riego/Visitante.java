package es.juanjovera.chuletas.jardin.riego;

import java.util.Random;

/**
 * Cada visitante es un hilo: llega, entra (o espera en la puerta), pasea y sale.
 * Igual que en jardin/semaforo: toda la sincronización está en Jardin.
 */
public class Visitante implements Runnable {
    private final int id;
    private final Jardin jardin;
    private final Random azar = new Random();

    public Visitante(int id, Jardin jardin) {
        this.id = id;
        this.jardin = jardin;
    }

    public int getId() {
        return id;
    }

    @Override
    public void run() {
        try {
            jardin.entrar(this);                     // puede quedarse esperando aquí dentro
            Thread.sleep(azar.nextInt(1000, 3001));  // pasea entre 1 y 3 s (fuera de la sección crítica)
            jardin.salir(this);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Visitante " + id + ": interrumpido.");
        }
    }
}
