package es.juanjovera.chuletas.filosofos.semaforo;

import java.util.Random;

/**
 * Cada filósofo es un hilo: piensa, coge sus dos tenedores, come y los suelta. Lo repite COMIDAS veces.
 */
public class Filosofo implements Runnable {
    public static final int COMIDAS = 3;
    private final int id;
    private final Mesa mesa;
    private final Random azar = new Random();

    public Filosofo(int id, Mesa mesa) {
        this.id = id;
        this.mesa = mesa;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= COMIDAS; i++) {
                System.out.println("Filósofo " + id + ": Pensando...");
                Thread.sleep(azar.nextInt(500, 1501));   // piensa SIN tenedores
                mesa.cogerTenedores(id);                // puede quedarse esperando aquí dentro
                System.out.println("Filósofo " + id + ": Comiendo (" + i + "/" + COMIDAS + ").");
                Thread.sleep(azar.nextInt(500, 1001));   // come CON los dos tenedores
                mesa.soltarTenedores(id);
            }
            System.out.println("Filósofo " + id + ": Ha terminado.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
