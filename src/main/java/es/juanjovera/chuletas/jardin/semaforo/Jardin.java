package es.juanjovera.chuletas.jardin.semaforo;

import java.util.concurrent.Semaphore;

/**
 * Recurso compartido: el jardín. Versión con SEMÁFOROS (java.util.concurrent.Semaphore).
 *
 * Dos semáforos, cada uno con un trabajo:
 * - aforo (5 permisos) = plazas libres. acquire() coge una; si no quedan, el hilo se duerme ahí (sin gastar
 *   CPU) hasta que alguien haga release() al salir.
 * - mutex (1 permiso) = cerrojo para tocar el contador "dentro" de uno en uno (exclusión mutua).
 *
 * REGLA DE ORO: nunca te bloquees en aforo.acquire() con el mutex cogido. Si te duermes con el cerrojo,
 * el que quiere salir no puede coger el mutex para devolverte la plaza -> interbloqueo (deadlock).
 */
public class Jardin {
    private static final int MAX_PLAZAS = 5;
    private final Semaphore aforo = new Semaphore(MAX_PLAZAS);
    private final Semaphore mutex = new Semaphore(1);
    private int dentro = 0; // solo para las trazas; se toca siempre con el mutex cogido

    public void entrar(Visitante v) throws InterruptedException {
        System.out.println("Visitante " + v.getId() + ": Llega a la puerta.");
        // tryAcquire(): mira si hay plaza Y la coge en UN solo paso, sin bloquearse. Devuelve false si no
        // había. Así nadie te quita la plaza entre mirar y coger.
        if (!aforo.tryAcquire()) {
            System.out.println("Visitante " + v.getId() + ": Espera en la puerta (jardín lleno).");
            aforo.acquire(); // se duerme aquí, FUERA del mutex, hasta que salga alguien
        }
        // Ya tiene plaza. Ahora sí, el cerrojo para el contador.
        mutex.acquire(); // ANTES del try: si me interrumpen aquí, el finally no suelta un cerrojo que no tengo
        try {
            dentro++;
            System.out.println("Visitante " + v.getId() + ": Entra (dentro: " + dentro + "/" + MAX_PLAZAS + ").");
        } finally {
            mutex.release(); // en finally: se suelta pase lo que pase
        }
    }

    public void salir(Visitante v) throws InterruptedException {
        mutex.acquire();
        try {
            dentro--;
            System.out.println("Visitante " + v.getId() + ": Sale (dentro: " + dentro + "/" + MAX_PLAZAS + ").");
            // La plaza se devuelve DESPUÉS del mensaje y con el mutex cogido: el que esperaba necesita el
            // mutex para escribir "Entra", así que su línea siempre sale detrás de este "Sale".
            aforo.release();
        } finally {
            mutex.release();
        }
    }
}
