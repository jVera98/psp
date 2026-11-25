package es.juanjovera.chuletas.procesos.entrada;

import es.juanjovera.chuletas.procesos.Lanzador;
import es.juanjovera.chuletas.procesos.hijos.Eco;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * 5.4 ENVIAR DATOS AL HIJO por su entrada estándar: getOutputStream().
 * Es el ejercicio 3 de la relación del profesor («enviar "Hola Manuel" a EjemploLectura»).
 *
 * Argumentos o entrada estándar:
 * - argumentos del main: se fijan al lanzar el proceso y no cambian (ejercicio 4 de la relación, Repetidor).
 * - entrada estándar: el padre puede ir mandando datos mientras el hijo está vivo.
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        // 1) Una sola línea, con bytes, como la solución del profesor
        System.out.println("--- 1) Una línea con OutputStream ---");
        Process hijo = new ProcessBuilder(Lanzador.java(Eco.class)).start();
        OutputStream os = hijo.getOutputStream();
        os.write("Manuel\n".getBytes(Lanzador.CHARSET_JAVA)); // el \n es el Intro: sin él, readLine() espera
        os.flush();  // vacía el búfer: si no, el dato puede quedarse en el padre y no llegar
        os.close();  // cierra la entrada del hijo: su readLine() devuelve null y termina
        leerSalida(hijo);

        // 2) Varias líneas con PrintWriter (más cómodo) y «Fin» para que el hijo pare
        System.out.println("\n--- 2) Varias líneas con PrintWriter ---");
        hijo = new ProcessBuilder(Lanzador.java(Eco.class)).start();
        try (PrintWriter alHijo = new PrintWriter(hijo.getOutputStream(), true, Lanzador.CHARSET_JAVA)) {
            for (String nombre : List.of("Elena", "Diego", "Fin")) {
                alHijo.println(nombre); // autoFlush = true: cada println se envía al momento
            }
        } // al cerrar el PrintWriter se cierra también la entrada del hijo
        leerSalida(hijo);
    }

    private static void leerSalida(Process hijo) throws IOException, InterruptedException {
        try (BufferedReader delHijo = new BufferedReader(
                new InputStreamReader(hijo.getInputStream(), Lanzador.CHARSET_JAVA))) {
            String linea;
            while ((linea = delHijo.readLine()) != null) {
                System.out.println("  hijo> " + linea);
            }
        }
        System.out.println("Padre: el hijo ha terminado con código " + hijo.waitFor());
    }
}
