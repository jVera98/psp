package es.juanjovera.chuletas.procesos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * PADRE. Una estación meteorológica: todo lo de procesos del tema en un solo ejercicio, con ProcessBuilder.
 *
 *   1. Lanza 3 sensores (hijos) EN PARALELO, cada uno con su salida y sus errores redirigidos a ficheros.
 *      El sensor C recibe "seis" en vez de un número: falla a propósito para ver el camino del error.
 *   2. Comprueba con un comando del sistema operativo que los hijos son procesos reales.
 *   3. Espera a cada hijo (con tiempo máximo y destroy si se cuelga) y mira su código de salida.
 *   4. Lee los ficheros de los que han ido bien y muestra un resumen por sensor.
 *   5. Manda todas las temperaturas a otro hijo por su ENTRADA estándar y lee lo que le contesta.
 *
 * Los ficheros se crean en "ficheros/", dentro de la carpeta del proyecto.
 */
public class Main {
    private static final File CARPETA = new File("ficheros");
    private static final String[] SENSORES = {"A", "B", "C"};
    private static final String[] LECTURAS = {"6", "6", "seis"};

    public static void main(String[] args) throws IOException, InterruptedException {
        CARPETA.mkdirs();
        System.out.println("Padre: soy el PID " + ProcessHandle.current().pid());

        // ---------- 1. CREAR los hijos y REDIRIGIR sus salidas. Todos los start() antes de esperar a nadie ----------
        Process[] hijos = new Process[SENSORES.length];
        for (int i = 0; i < SENSORES.length; i++) {
            ProcessBuilder pb = new ProcessBuilder(comandoJava("Sensor", SENSORES[i], LECTURAS[i]));
            pb.redirectOutput(salida(i));   // System.out del hijo → sensor_X.txt (se vacía antes)
            pb.redirectError(errores(i));   // System.err del hijo → errores_X.txt
            hijos[i] = pb.start();          // aquí nace el proceso; si el programa no existe: IOException
            System.out.println("Padre: lanzado el sensor " + SENSORES[i] + " (PID " + hijos[i].pid()
                    + ", vivo: " + hijos[i].isAlive() + ")");
        }

        // ---------- 2. Un COMANDO del SO: los hijos aparecen como procesos java ----------
        System.out.println("\nPadre: procesos java en el sistema ahora mismo:");
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        ProcessBuilder listar = windows
                ? new ProcessBuilder("tasklist", "/FI", "IMAGENAME eq java.exe", "/NH") // /NH = sin cabecera
                : new ProcessBuilder("ps", "-C", "java", "-o", "pid,ppid,cmd", "--no-headers");
        listar.inheritIO();                 // su salida, directa a la consola del padre
        listar.start().waitFor();

        // ---------- 3. ESPERAR a cada hijo y comprobar su CÓDIGO DE SALIDA ----------
        System.out.println();
        boolean[] correcto = new boolean[SENSORES.length];
        for (int i = 0; i < SENSORES.length; i++) {
            if (!hijos[i].waitFor(10, TimeUnit.SECONDS)) { // false = sigue vivo tras 10 s
                hijos[i].destroy();
                hijos[i].waitFor();
                System.out.println("Padre: el sensor " + SENSORES[i] + " no respondía; lo he destruido");
            }
            int codigo = hijos[i].exitValue();             // ya ha terminado: no lanza excepción
            correcto[i] = codigo == 0;
            System.out.println("Padre: el sensor " + SENSORES[i] + " ha terminado con código " + codigo);
        }

        // ---------- 4. LEER los ficheros y CONSOLIDAR ----------
        System.out.println("\n===== RESUMEN =====");
        List<String> temperaturas = new ArrayList<>();
        int total = 0;
        for (int i = 0; i < SENSORES.length; i++) {
            if (!correcto[i]) {
                System.out.println("Sensor " + SENSORES[i] + ": ERROR -> " + primeraLinea(errores(i)));
                continue;
            }
            int lineas = 0;
            try (BufferedReader lector = new BufferedReader(new FileReader(salida(i)))) {
                String linea;
                while ((linea = lector.readLine()) != null) {
                    temperaturas.add(linea.split(";")[2]);    // «A;3;27» → "27"
                    lineas++;
                }
            }
            total += lineas;
            System.out.println("Sensor " + SENSORES[i] + ": " + lineas + " lecturas");
        }
        System.out.println("Total: " + total + " lecturas");

        // ---------- 5. Comunicarse con un hijo: ESCRIBIR en su entrada y LEER su salida ----------
        System.out.println("\nPadre: mando las " + temperaturas.size() + " temperaturas a la calculadora");
        ProcessBuilder pbCalculadora = new ProcessBuilder(comandoJava("Calculadora"));
        pbCalculadora.redirectError(ProcessBuilder.Redirect.INHERIT); // sus errores, a la consola del padre
        Process calculadora = pbCalculadora.start();

        try (PrintWriter alHijo = new PrintWriter(calculadora.getOutputStream())) { // → System.in del hijo
            for (String temperatura : temperaturas) {
                alHijo.println(temperatura);
            }
        } // close() envía lo pendiente y cierra su entrada: su readLine() devuelve null y termina de leer

        try (BufferedReader delHijo = new BufferedReader(
                new InputStreamReader(calculadora.getInputStream()))) {             // ← System.out del hijo
            String linea;
            while ((linea = delHijo.readLine()) != null) {
                System.out.println("  calculadora> " + linea);
            }
        } // primero leer y DESPUÉS waitFor: si el hijo escribe mucho y nadie le lee, se bloquean los dos
        System.out.println("Padre: la calculadora ha terminado con código " + calculadora.waitFor());
    }

    /**
     * Comando para lanzar una clase de este paquete como proceso hijo:
     *   <java del padre> -cp <classpath del padre> es.juanjovera.chuletas.procesos.<Clase> <argumentos...>
     * Una palabra en cada String: ProcessBuilder no trocea por espacios.
     */
    private static List<String> comandoJava(String clase, String... argumentos) {
        List<String> comando = new ArrayList<>();
        comando.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        comando.add("-cp");
        comando.add(System.getProperty("java.class.path"));
        comando.add("es.juanjovera.chuletas.procesos." + clase); // nombre COMPLETO, con el paquete
        comando.addAll(List.of(argumentos));
        return comando;
    }

    private static File salida(int i) {
        return new File(CARPETA, "sensor_" + SENSORES[i] + ".txt");
    }

    private static File errores(int i) {
        return new File(CARPETA, "errores_" + SENSORES[i] + ".txt");
    }

    private static String primeraLinea(File fichero) throws IOException {
        try (BufferedReader lector = new BufferedReader(new FileReader(fichero))) {
            String linea = lector.readLine();
            return linea == null ? "(sin mensaje)" : linea;
        }
    }
}
