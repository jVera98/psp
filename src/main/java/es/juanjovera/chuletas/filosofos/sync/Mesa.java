package es.juanjovera.chuletas.filosofos.sync;

/**
 * Recurso compartido: la mesa con los tenedores. Versión con MONITOR (synchronized + wait + notifyAll).
 *
 * El interbloqueo clásico: si cada filósofo coge primero su tenedor izquierdo y luego espera el derecho,
 * puede pasar que los 5 tengan el izquierdo a la vez y esperen el derecho para siempre (cada uno retiene
 * un tenedor y espera otro: "retener y esperar" + "espera circular").
 *
 * Solución con monitor: coger LOS DOS tenedores a la vez o ninguno. Dentro de un método synchronized el
 * filósofo mira si están libres los dos; si no, espera (wait) sin tener ninguno. Nadie retiene un tenedor
 * mientras espera, así que no hay interbloqueo.
 */
public class Mesa {
    private final boolean[] tenedorOcupado; // dato compartido: lo protege synchronized
    private final int n;

    public Mesa(int n) {
        this.n = n;
        this.tenedorOcupado = new boolean[n]; // todos a false = libres
    }

    public synchronized void cogerTenedores(int id) throws InterruptedException {
        int izq = id;
        int der = (id + 1) % n; // el % hace la mesa redonda: el 4 usa el 4 y el 0
        // WHILE: al despertar vuelve a mirar, otro vecino pudo coger un tenedor antes
        while (tenedorOcupado[izq] || tenedorOcupado[der]) {
            wait(); // suelta el cerrojo de la mesa para que los demás puedan soltar tenedores
        }
        tenedorOcupado[izq] = true; // los dos en el mismo paso: nadie se cuela en medio
        tenedorOcupado[der] = true;
        System.out.println("Filósofo " + id + ": Coge los tenedores " + izq + " y " + der + ".");
    }

    public synchronized void soltarTenedores(int id) {
        int izq = id;
        int der = (id + 1) % n;
        tenedorOcupado[izq] = false;
        tenedorOcupado[der] = false;
        System.out.println("Filósofo " + id + ": Suelta los tenedores " + izq + " y " + der + ".");
        notifyAll(); // los vecinos que esperaban vuelven a mirar si ya tienen sus dos tenedores
    }
}
