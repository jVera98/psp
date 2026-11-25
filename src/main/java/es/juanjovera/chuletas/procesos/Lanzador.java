package es.juanjovera.chuletas.procesos;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Lo que se repite en todos los ejemplos de procesos.
 *
 * Para lanzar como hijo OTRA clase con main de ESTE proyecto hace falta el comando completo:
 *
 *     <ruta del java> -cp <dónde están los .class> <paquete.Clase> <argumentos...>
 *
 * - java.home: la carpeta del JDK con el que se está ejecutando el padre. Así el hijo usa el mismo Java,
 *   aunque "java" no esté en el PATH o allí haya otra versión.
 * - java.class.path: las carpetas donde están los .class del padre (target/classes en IntelliJ y NetBeans).
 *   Con él, el hijo encuentra sus clases sin depender de la carpeta desde la que se lance.
 * - getName(): el nombre COMPLETO de la clase, con su paquete (es.juanjovera...Saludo, no "Saludo").
 *
 * En el examen se puede escribir a mano, como en crear/Main:
 *     new ProcessBuilder(System.getProperty("java.home") + "/bin/java", "-cp",
 *             System.getProperty("java.class.path"), "paquete.Clase", "arg1");
 */
public final class Lanzador {

    /** true si estamos en Windows: hay comandos (dir, tasklist) que cambian según el sistema operativo. */
    public static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    /**
     * Juego de caracteres con el que escribe un hijo Java cuando su salida va a una tubería o a un fichero
     * (en Windows, Cp1252; en Linux, UTF-8). Hay que leerlo con este mismo: si no, las tildes salen rotas.
     */
    public static final Charset CHARSET_JAVA = Charset.forName(System.getProperty("native.encoding"));

    /** Juego de caracteres de los comandos de consola: cmd.exe escribe en IBM850; Linux, en UTF-8. */
    public static final Charset CHARSET_CONSOLA = WINDOWS ? Charset.forName("IBM850") : CHARSET_JAVA;

    private Lanzador() {
    }

    /** Comando para lanzar {@code clase} como proceso hijo, con el mismo Java y el mismo classpath. */
    public static List<String> java(Class<?> clase, String... argumentos) {
        List<String> comando = new ArrayList<>();
        comando.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        comando.add("-cp");
        comando.add(classpathAbsoluto());
        comando.add(clase.getName());
        comando.addAll(List.of(argumentos));
        return comando;
    }

    /**
     * El classpath con rutas absolutas. IntelliJ y NetBeans ya lo dan así, pero si el padre se lanza con
     * "java -cp out ..." la ruta es relativa y, si el hijo trabaja en otra carpeta (directory()), no
     * encuentra sus clases: «Could not find or load main class».
     */
    private static String classpathAbsoluto() {
        StringJoiner rutas = new StringJoiner(File.pathSeparator);
        for (String ruta : System.getProperty("java.class.path").split(File.pathSeparator)) {
            rutas.add(Path.of(ruta).toAbsolutePath().toString());
        }
        return rutas.toString();
    }
}
