package es.juanjovera.chuletas.procesos.redireccion;

import es.juanjovera.chuletas.procesos.Lanzador;
import es.juanjovera.chuletas.procesos.hijos.Eco;
import es.juanjovera.chuletas.procesos.hijos.Saludo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

/**
 * REDIRECCIÓN DE FLUJOS: que el hijo lea de un fichero o escriba en un fichero sin que el padre haga de
 * intermediario. Lo configura el ProcessBuilder ANTES de start().
 *
 *     redirectOutput(File)                 salida estándar → fichero (lo vacía y escribe desde cero)
 *     redirectOutput(Redirect.appendTo(f)) salida estándar → fichero, AÑADIENDO al final
 *     redirectError(File)                  salida de error → fichero
 *     redirectInput(File)                  fichero → entrada estándar
 *     redirectErrorStream(true)            la salida de error va por el mismo sitio que la estándar
 *     inheritIO()                          las tres, a la consola del padre
 *
 * Los ficheros se crean en la carpeta "ficheros/" dentro de la carpeta de trabajo (la del proyecto).
 * Una ruta relativa se resuelve desde la carpeta de trabajo del PADRE (user.dir).
 */
public class Main {
    private static final File CARPETA = new File("ficheros");

    public static void main(String[] args) throws IOException, InterruptedException {
        CARPETA.mkdirs();

        // 1) Salida a un fichero (sobrescribe)
        System.out.println("--- 1) redirectOutput: salida a fichero ---");
        File salida = new File(CARPETA, "saludos.txt");
        ProcessBuilder pb = new ProcessBuilder(Lanzador.java(Saludo.class, "Ana"));
        pb.redirectOutput(salida);
        int codigo = pb.start().waitFor(); // ahora sí se puede esperar primero: nadie tiene que leer tuberías
        mostrar(salida, Lanzador.CHARSET_JAVA, "código " + codigo);

        // 2) Añadir al final: cada ejecución deja su línea
        System.out.println("\n--- 2) Redirect.appendTo: añadir al fichero ---");
        for (String nombre : List.of("Luis", "Eva")) {
            new ProcessBuilder(Lanzador.java(Saludo.class, nombre))
                    .redirectOutput(ProcessBuilder.Redirect.appendTo(salida))
                    .start().waitFor();
        }
        mostrar(salida, Lanzador.CHARSET_JAVA, "tras dos ejecuciones más");

        // 3) Ejercicio 2 de la relación: los errores de un comando que falla, a "errores.txt"
        System.out.println("\n--- 3) redirectError: errores a fichero ---");
        File errores = new File(CARPETA, "errores.txt");
        ProcessBuilder falla = Lanzador.WINDOWS
                ? new ProcessBuilder("cmd", "/c", "dir", "C:\\carpeta_que_no_existe")
                : new ProcessBuilder("ls", "/carpeta_que_no_existe");
        falla.redirectError(errores);
        codigo = falla.start().waitFor();
        mostrar(errores, Lanzador.CHARSET_CONSOLA, "código " + codigo + " (≠ 0: el comando ha fallado)");

        // 4) Un programa que NO existe ni siquiera llega a ser proceso: start() lanza IOException
        System.out.println("\n--- 4) Programa inexistente ---");
        try {
            new ProcessBuilder("programa_que_no_existe").start();
        } catch (IOException e) {
            System.out.println("Padre: start() ha lanzado IOException: " + e.getMessage());
        }

        // 5) Entrada desde un fichero: Eco lee las líneas como si las tecleara alguien
        System.out.println("\n--- 5) redirectInput: entrada desde fichero ---");
        File entrada = new File(CARPETA, "nombres.txt");
        Files.write(entrada.toPath(), List.of("Pepe", "Amalia", "Fin", "Esta ya no la lee"), Lanzador.CHARSET_JAVA);
        new ProcessBuilder(Lanzador.java(Eco.class))
                .inheritIO()                 // inheritIO pone las TRES a la consola...
                .redirectInput(entrada)      // ...y esto cambia solo la entrada (por eso va DESPUÉS)
                .start().waitFor();
    }

    private static void mostrar(File fichero, Charset charset, String nota) throws IOException {
        System.out.println("Padre: " + fichero + " (" + nota + "):");
        for (String linea : Files.readAllLines(fichero.toPath(), charset)) {
            System.out.println("  | " + linea);
        }
    }
}
