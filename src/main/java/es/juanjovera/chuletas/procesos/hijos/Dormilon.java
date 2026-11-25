package es.juanjovera.chuletas.procesos.hijos;

/**
 * HIJO. Tarda mucho (30 s) en terminar: sirve para ver isAlive(), waitFor con tiempo máximo y destroy().
 */
public class Dormilon {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Dormilon: me echo 30 segundos.");
        Thread.sleep(30_000);
        System.out.println("Dormilon: ya me he despertado."); // si lo destruyen antes, esto no sale nunca
    }
}
