package es.juanjovera.chuletas.barbero.semaforo;

import java.util.concurrent.Semaphore;

/**
 * Cada cliente es un hilo: llega; si hay sitio se queda hasta que lo pelan; si no, se marcha.
 */
public class Cliente implements Runnable {
    private final int id;
    private final Barberia barberia;
    // Semáforo de SEÑAL, uno por cliente, empieza a 0: el cliente se duerme en acquire() hasta que el barbero
    // haga release() al terminar con ÉL (y no con otro).
    private final Semaphore pelado = new Semaphore(0);

    public Cliente(int id, Barberia barberia) {
        this.id = id;
        this.barberia = barberia;
    }

    public int getId() {
        return id;
    }

    void corteTerminado() { // lo llama el barbero
        pelado.release();
    }

    @Override
    public void run() {
        try {
            if (barberia.entrar(this)) { // false = sala llena, se ha marchado
                pelado.acquire();        // se queda hasta que el barbero termine con él
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
