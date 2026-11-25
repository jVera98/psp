package es.juanjovera.chuletas.jardin.sync;

/**
 * Recurso compartido: el jardín. Versión con MONITOR (synchronized + wait + notifyAll).
 *
 * - synchronized en el método: el propio objeto Jardin es el cerrojo. Solo un hilo a la vez puede estar
 *   dentro de entrar() o salir(). Por eso comprobar el aforo y sumar al contador van juntos, sin que nadie
 *   se cuele en medio (no hay "comprobar fuera y actuar dentro").
 * - wait(): SUELTA el cerrojo y duerme (sin gastar CPU) hasta que otro haga notifyAll(). Al despertar,
 *   vuelve a coger el cerrojo antes de seguir. Solo se puede llamar dentro de algo synchronized.
 * - notifyAll(): despierta a TODOS los que están en wait() sobre este objeto; cada uno vuelve a mirar
 *   su condición.
 */
public class Jardin {
    private static final int MAX_PLAZAS = 5;
    private int dentro = 0; // dato compartido: lo protege synchronized

    public synchronized void entrar(Visitante v) throws InterruptedException {
        System.out.println("Visitante " + v.getId() + ": Llega a la puerta.");
        if (dentro == MAX_PLAZAS) {
            // El mensaje va fuera del while para que salga UNA sola vez
            System.out.println("Visitante " + v.getId() + ": Espera en la puerta (jardín lleno).");
        }
        // WHILE, no if: al despertar hay que volver a comprobar. notifyAll despierta a todos y otro
        // visitante pudo coger la plaza antes que yo.
        while (dentro == MAX_PLAZAS) {
            wait();
        }
        dentro++;
        System.out.println("Visitante " + v.getId() + ": Entra (dentro: " + dentro + "/" + MAX_PLAZAS + ").");
    }

    public synchronized void salir(Visitante v) {
        dentro--;
        System.out.println("Visitante " + v.getId() + ": Sale (dentro: " + dentro + "/" + MAX_PLAZAS + ").");
        notifyAll(); // ha quedado una plaza libre: que los de la puerta vuelvan a mirar
    }
}
