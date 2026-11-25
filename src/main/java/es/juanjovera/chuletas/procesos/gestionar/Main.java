package es.juanjovera.chuletas.procesos.gestionar;

import es.juanjovera.chuletas.procesos.Lanzador;
import es.juanjovera.chuletas.procesos.hijos.Dormilon;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 5.4 GESTIONAR UN PROCESO con los métodos de Process: pid, isAlive, waitFor, exitValue, destroy.
 * Es lo que hace el menú del tutorial «GeneradorProcesos» del profesor (lanzar, listar vivos, finalizar).
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(Lanzador.java(Dormilon.class));
        pb.inheritIO();
        Process hijo = pb.start();
        System.out.println("Padre: Dormilon lanzado, PID " + hijo.pid() + ", ¿vivo? " + hijo.isAlive());

        // Los hijos de este proceso, vistos desde ProcessHandle (lo que enseña el árbol de Process Explorer)
        ProcessHandle.current().children()
                .forEach(h -> System.out.println("Padre: tengo un hijo con PID " + h.pid()));

        // exitValue() NO espera: si el hijo sigue vivo lanza IllegalThreadStateException
        try {
            hijo.exitValue();
        } catch (IllegalThreadStateException e) {
            System.out.println("Padre: exitValue() con el hijo vivo -> IllegalThreadStateException");
        }

        // waitFor con tiempo máximo: devuelve true si terminó a tiempo y false si sigue vivo
        boolean terminado = hijo.waitFor(2, TimeUnit.SECONDS);
        System.out.println("Padre: tras esperar 2 s, ¿ha terminado? " + terminado);

        if (!terminado) {
            hijo.destroy();          // pide que termine (en Linux, señal SIGTERM = kill <pid>)
            // hijo.destroyForcibly(); // lo mata sin contemplaciones (SIGKILL = kill -9 <pid>)
            int codigo = hijo.waitFor(); // espera a que muera de verdad antes de preguntar
            System.out.println("Padre: lo he destruido. ¿vivo? " + hijo.isAlive() + " · código " + codigo
                    + " (Windows da 1; Linux, 143 = 128 + SIGTERM)");
        }
    }
}
