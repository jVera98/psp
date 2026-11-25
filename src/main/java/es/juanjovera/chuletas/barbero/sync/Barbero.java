package es.juanjovera.chuletas.barbero.sync;

import java.util.Random;

/**
 * El hilo del barbero: bucle infinito "coger siguiente cliente -> pelar -> avisar de que ha terminado".
 * Si no hay nadie, se duerme dentro de barberia.siguienteCliente().
 */
public class Barbero implements Runnable {
    private final Barberia barberia;
    private final Random azar = new Random();

    public Barbero(Barberia barberia) {
        this.barberia = barberia;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Cliente cliente = barberia.siguienteCliente(); // se duerme aquí si no hay nadie
                System.out.println("Barbero: Cortando el pelo al Cliente " + cliente.getId() + "...");
                Thread.sleep(azar.nextInt(1000, 2501));         // el corte, FUERA de la sección crítica
                System.out.println("Barbero: Ha terminado con el Cliente " + cliente.getId() + ".");
                barberia.terminarCorte(cliente);               // avisa a ESE cliente para que se vaya
            }
        } catch (InterruptedException e) {
            // Lo interrumpe el main cuando ya no quedan clientes: así sale del while (true)
            System.out.println("Barbero: Cierra la barbería."); // (mensaje extra, no está en el enunciado)
        }
    }
}
