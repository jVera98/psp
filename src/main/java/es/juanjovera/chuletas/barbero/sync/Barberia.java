package es.juanjovera.chuletas.barbero.sync;

import java.util.ArrayDeque;

/**
 * Recurso compartido: la barbería. Versión con MONITOR (synchronized + wait + notifyAll).
 *
 * Todos los métodos son synchronized sobre el MISMO objeto: comprobar (¿duerme el barbero?, ¿hay sillas?)
 * y actuar (sentarse) ocurren sin que nadie se cuele en medio.
 *
 * Datos compartidos (todos protegidos por el cerrojo de este objeto):
 * - salaEspera: los que esperan sentados, en orden de llegada (su tamaño = sillas ocupadas);
 * - sillaCorte: el cliente que ha despertado al barbero (va directo a la silla, no pasa por la sala);
 * - barberoDormido.
 *
 * Regla segura con monitores: cada vez que cambias algo por lo que otro hilo puede estar esperando,
 * notifyAll(); y quien espera, siempre con while (condición) wait();
 */
public class Barberia {
    private final int sillas;
    private final ArrayDeque<Cliente> salaEspera = new ArrayDeque<>();
    private Cliente sillaCorte = null;
    private boolean barberoDormido = false;

    public Barberia(int sillas) {
        this.sillas = sillas;
    }

    /** Lo llama el CLIENTE. Devuelve false si se marcha porque la sala está llena. */
    public synchronized boolean entrar(Cliente c) {
        System.out.println("Cliente " + c.getId() + ": Ha llegado a la barbería.");
        if (barberoDormido) {
            // Lo despierta y se sienta directamente en la silla de corte
            barberoDormido = false;
            sillaCorte = c;
            notifyAll(); // el barbero está en wait() dentro de siguienteCliente()
            return true;
        }
        if (salaEspera.size() < sillas) {
            salaEspera.add(c);
            System.out.println("Cliente " + c.getId() + ": Esperando en la sala de espera (Sillas ocupadas: "
                    + salaEspera.size() + "/" + sillas + ").");
            notifyAll();
            return true;
        }
        System.out.println("Cliente " + c.getId() + ": Se marcha porque la sala de espera está llena.");
        return false;
    }

    /** Lo llama el BARBERO: devuelve el siguiente cliente; si no hay ninguno, se duerme hasta que llegue. */
    public synchronized Cliente siguienteCliente() throws InterruptedException {
        if (nadieEsperando()) {
            barberoDormido = true;
            System.out.println("Barbero: Durmiendo...");
        }
        while (nadieEsperando()) {
            wait(); // suelta el cerrojo: así los clientes pueden entrar en entrar() y despertarlo
        }
        if (sillaCorte != null) { // el que lo despertó ya está en la silla
            Cliente c = sillaCorte;
            sillaCorte = null;
            return c;
        }
        return salaEspera.poll(); // el primero de la sala pasa a la silla (deja una silla libre)
    }

    /** Lo llama el CLIENTE: espera hasta que el barbero termine con él. */
    public synchronized void esperarCorte(Cliente c) throws InterruptedException {
        while (!c.isPelado()) {
            wait();
        }
    }

    /** Lo llama el BARBERO al acabar un corte. */
    public synchronized void terminarCorte(Cliente c) {
        c.setPelado();
        notifyAll(); // despierta a todos; solo sale de su while el cliente que ya está pelado
    }

    private boolean nadieEsperando() { // solo se llama desde métodos synchronized
        return sillaCorte == null && salaEspera.isEmpty();
    }
}
