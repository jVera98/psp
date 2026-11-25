package es.juanjovera.chuletas.procesos.salida;

import es.juanjovera.chuletas.procesos.Lanzador;
import es.juanjovera.chuletas.procesos.hijos.Saludo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * 5.4 LEER LO QUE ESCRIBE EL HIJO: getInputStream() (su salida estándar) y getErrorStream() (su salida de error).
 *
 * Los nombres despistan: getInputStream() es la ENTRADA del padre, por donde le llega la SALIDA del hijo.
 *
 *     hijo: System.out ──► p.getInputStream()  (padre lee)
 *     hijo: System.err ──► p.getErrorStream()  (padre lee)
 *     hijo: System.in  ◄── p.getOutputStream() (padre escribe; ver entrada/Main)
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        // 1) Ejercicio 1 de la relación: el mismo hijo con nombre y sin él. ¿Qué código devuelve en cada caso?
        System.out.println("--- 1) Saludo con nombre y sin nombre ---");
        ejecutarSaludo("Fran");
        ejecutarSaludo();

        // 2) Un comando del sistema: leer su salida línea a línea y contarlas.
        //    dir es una orden INTERNA de cmd (no hay un dir.exe): hay que lanzarla a través de "cmd /c".
        System.out.println("\n--- 2) Salida de un comando del sistema ---");
        ProcessBuilder pb = Lanzador.WINDOWS
                ? new ProcessBuilder("cmd", "/c", "dir")
                : new ProcessBuilder("ls", "-l");
        pb.redirectErrorStream(true);  // mezcla la salida de error con la estándar: un solo flujo que leer
        Process p = pb.start();
        int lineas = 0;
        try (BufferedReader salida = lector(p.getInputStream(), Lanzador.CHARSET_CONSOLA)) {
            String linea;
            while ((linea = salida.readLine()) != null) { // null = el hijo ha cerrado su salida (ha terminado)
                System.out.println("  | " + linea);
                lineas++;
            }
        }
        System.out.println("Padre: " + lineas + " líneas leídas; código de salida " + p.waitFor());
    }

    private static void ejecutarSaludo(String... argumentos) throws IOException, InterruptedException {
        Process hijo = new ProcessBuilder(Lanzador.java(Saludo.class, argumentos)).start();

        // Primero LEER y después waitFor(): si el hijo escribe mucho y nadie le lee, se llena la tubería,
        // el hijo se queda bloqueado escribiendo y el padre esperando para siempre.
        String salida = leerTodo(hijo.getInputStream());
        String errores = leerTodo(hijo.getErrorStream());
        int codigo = hijo.waitFor();

        System.out.println("Saludo " + String.join(" ", argumentos) + " -> código " + codigo
                + " · salida: «" + salida.strip() + "» · errores: «" + errores.strip() + "»");
    }

    /** Lee un flujo entero. try-with-resources lo cierra al acabar. */
    private static String leerTodo(InputStream flujo) throws IOException {
        StringBuilder texto = new StringBuilder();
        try (BufferedReader lector = lector(flujo, Lanzador.CHARSET_JAVA)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                texto.append(linea).append('\n');
            }
        }
        return texto.toString();
    }

    /** BufferedReader sobre lo que escribe un hijo, con el juego de caracteres con el que escribe. */
    private static BufferedReader lector(InputStream flujo, Charset charset) {
        return new BufferedReader(new InputStreamReader(flujo, charset));
    }
}
