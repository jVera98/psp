package es.juanjovera.chuletas.filosofos.semaforo;

import java.util.concurrent.Semaphore;

/**
 * Recurso compartido: la mesa con los tenedores. Versión con SEMÁFOROS.
 *
 * Cada tenedor es un Semaphore(1): acquire() = cogerlo (si lo tiene el vecino, espera); release() = soltarlo.
 *
 * El interbloqueo clásico: si TODOS cogen primero el izquierdo y luego el derecho, puede pasar que los 5
 * tengan el izquierdo y esperen el derecho para siempre: una espera en círculo (0 espera al 1, 1 al 2...
 * y 4 al 0).
 *
 * Solución usada aquí: ORDENAR LOS RECURSOS. Cada filósofo coge primero el tenedor de número MENOR. Los
 * filósofos 0 a 3 cogen izquierdo y luego derecho, pero el 4 (tenedores 4 y 0) coge primero el 0. Así el
 * círculo se rompe: siempre queda al menos uno que puede coger sus dos tenedores.
 *
 * Otra solución válida: un semáforo "comedor" con n-1 permisos (como mucho 4 sentados a la vez); con uno
 * de pie, siempre hay alguien con sus dos tenedores libres.
 */
public class Mesa {
    private final Semaphore[] tenedores;
    private final int n;

    public Mesa(int n) {
        this.n = n;
        this.tenedores = new Semaphore[n];
        for (int i = 0; i < n; i++) {
            tenedores[i] = new Semaphore(1); // 1 permiso = tenedor libre
        }
    }

    public void cogerTenedores(int id) throws InterruptedException {
        int izq = id;
        int der = (id + 1) % n;              // el % hace la mesa redonda: el 4 usa el 4 y el 0
        int primero = Math.min(izq, der);    // siempre el de número menor primero
        int segundo = Math.max(izq, der);
        tenedores[primero].acquire();
        try {
            tenedores[segundo].acquire();
        } catch (InterruptedException e) {
            tenedores[primero].release();    // si me interrumpen esperando el segundo, no me quedo el primero
            throw e;
        }
        System.out.println("Filósofo " + id + ": Coge los tenedores " + izq + " y " + der + ".");
    }

    public void soltarTenedores(int id) {
        int izq = id;
        int der = (id + 1) % n;
        // El mensaje ANTES de soltar: si soltara primero, el vecino podría escribir "Coge" antes que yo "Suelta"
        System.out.println("Filósofo " + id + ": Suelta los tenedores " + izq + " y " + der + ".");
        tenedores[izq].release();
        tenedores[der].release();
    }
}
