package es.juanjovera.chuletas.barbero.semaforo;

import java.util.ArrayDeque;
import java.util.concurrent.Semaphore;

/**
 * Recurso compartido: la barbería. Versión con SEMÁFOROS.
 *
 * - mutex (1 permiso): cerrojo para los datos compartidos (salaEspera, sillaCorte, barberoDormido).
 *   Comprobar (¿duerme?, ¿hay sillas?) y sentarse van dentro del MISMO mutex.acquire() ... release().
 * - hayClientes (empieza a 0): semáforo de SEÑAL que cuenta los clientes pendientes de pelar. Cada cliente
 *   que se sienta hace release(); el barbero hace acquire() y, si está a 0, se duerme ahí.
 * - La espera de cada cliente a que acaben con él es el semáforo "pelado" de Cliente.
 *
 * REGLA DE ORO: el barbero se bloquea en hayClientes.acquire() FUERA del mutex. Si se durmiera con el
 * mutex cogido, ningún cliente podría entrar a despertarlo -> interbloqueo.
 */
public class Barberia {
    private final int sillas;
    private final Semaphore mutex = new Semaphore(1);
    private final Semaphore hayClientes = new Semaphore(0);
    private final ArrayDeque<Cliente> salaEspera = new ArrayDeque<>(); // su tamaño = sillas ocupadas
    private Cliente sillaCorte = null;     // el que ha despertado al barbero (no pasa por la sala)
    private boolean barberoDormido = false;

    public Barberia(int sillas) {
        this.sillas = sillas;
    }

    /** Lo llama el CLIENTE. Devuelve false si se marcha porque la sala está llena. */
    public boolean entrar(Cliente c) throws InterruptedException {
        System.out.println("Cliente " + c.getId() + ": Ha llegado a la barbería.");
        mutex.acquire(); // antes del try: si me interrumpen aquí, el finally no suelta un cerrojo que no tengo
        try {
            if (barberoDormido) {
                barberoDormido = false; // lo despierta y se sienta directo en la silla de corte
                sillaCorte = c;
            } else if (salaEspera.size() < sillas) {
                salaEspera.add(c);
                System.out.println("Cliente " + c.getId() + ": Esperando en la sala de espera (Sillas ocupadas: "
                        + salaEspera.size() + "/" + sillas + ").");
            } else {
                System.out.println("Cliente " + c.getId() + ": Se marcha porque la sala de espera está llena.");
                return false; // el finally suelta el mutex igualmente
            }
            hayClientes.release(); // un cliente más pendiente: si el barbero dormía en acquire(), despierta
            return true;
        } finally {
            mutex.release();
        }
    }

    /** Lo llama el BARBERO: devuelve el siguiente cliente; si no hay ninguno, se duerme hasta que llegue. */
    public Cliente siguienteCliente() throws InterruptedException {
        mutex.acquire();
        try {
            if (sillaCorte == null && salaEspera.isEmpty()) {
                barberoDormido = true;
                System.out.println("Barbero: Durmiendo...");
            }
        } finally {
            mutex.release();
        }
        hayClientes.acquire(); // se duerme AQUÍ, sin el mutex, hasta que un cliente haga release()
        mutex.acquire();
        try {
            if (sillaCorte != null) { // el que lo despertó ya está en la silla
                Cliente c = sillaCorte;
                sillaCorte = null;
                return c;
            }
            return salaEspera.poll(); // el primero de la sala pasa a la silla (deja una silla libre)
        } finally {
            mutex.release();
        }
    }

    /** Lo llama el BARBERO al acabar un corte: despierta a ESE cliente. */
    public void terminarCorte(Cliente c) {
        c.corteTerminado();
    }
}
