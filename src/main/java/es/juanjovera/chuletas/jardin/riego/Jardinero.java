package es.juanjovera.chuletas.jardin.riego;

import java.util.Random;

/**
 * El jardinero es otro hilo: riega 3 veces, con una pausa de 2-4 s antes de cada riego, y termina.
 * Como los visitantes, solo llama a los métodos de Jardin.
 */
public class Jardinero implements Runnable {
    private static final int RIEGOS = 3;
    private final Jardin jardin;
    private final Random azar = new Random();

    public Jardinero(Jardin jardin) {
        this.jardin = jardin;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < RIEGOS; i++) {
                Thread.sleep(azar.nextInt(2000, 4001)); // la pausa es del jardinero, no del jardín
                jardin.regar();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Jardinero: interrumpido.");
        }
    }
}
