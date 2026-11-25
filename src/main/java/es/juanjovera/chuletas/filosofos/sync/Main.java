package es.juanjovera.chuletas.filosofos.sync;

/**
 * La cena de los filósofos: 5 filósofos alrededor de una mesa redonda con 5 tenedores, uno entre cada dos.
 * Para comer, un filósofo necesita los DOS tenedores que tiene al lado (izquierdo = su número,
 * derecho = el siguiente, y el 4 comparte el 0 con el filósofo 0). Cada uno come 3 veces.
 */
public class Main {
    private static final int FILOSOFOS = 5;

    public static void main(String[] args) throws InterruptedException {
        Mesa mesa = new Mesa(FILOSOFOS); // UN solo objeto compartido
        Thread[] hilos = new Thread[FILOSOFOS];

        for (int i = 0; i < hilos.length; i++) {
            hilos[i] = new Thread(new Filosofo(i, mesa));
            hilos[i].start(); // todos a la vez: es lo que provocaría el interbloqueo si estuviera mal
        }
        for (Thread hilo : hilos) {
            hilo.join();
        }
        System.out.println("Fin: todos los filósofos han comido " + Filosofo.COMIDAS + " veces.");
    }
}
