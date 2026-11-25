package es.juanjovera.chuletas.procesos.paralelo;

import es.juanjovera.chuletas.procesos.Lanzador;
import es.juanjovera.chuletas.procesos.hijos.Generador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * TODO JUNTO (el esquema del ejercicio 2 de la tarea): un padre coordina dos hijos que trabajan en PARALELO.
 *
 *   1. Crea los dos hijos con ProcessBuilder.
 *   2. Redirige la salida de cada uno a su propio fichero (y sus errores a otro).
 *   3. Lanza LOS DOS y solo después espera a cada uno, comprobando su código de salida.
 *   4. Con los dos terminados, lee los ficheros y muestra un resumen.
 *
 * El orden del paso 3 es lo que hace que sea paralelo:
 *   start(A); start(B); waitFor(A); waitFor(B);   → A y B trabajan a la vez
 *   start(A); waitFor(A); start(B); waitFor(B);   → B no empieza hasta que acaba A (secuencial)
 */
public class Main {
    private static final String[] NOMBRES = {"A", "B"};
    private static final int REGISTROS = 6;
    private static final File CARPETA = new File("ficheros");

    public static void main(String[] args) throws IOException, InterruptedException {
        CARPETA.mkdirs();
        Process[] hijos = new Process[NOMBRES.length];
        File[] salidas = new File[NOMBRES.length];

        // 1 y 2. Crear y redirigir; start() de todos SIN esperar todavía
        long inicio = System.currentTimeMillis();
        for (int i = 0; i < NOMBRES.length; i++) {
            salidas[i] = new File(CARPETA, "generador_" + NOMBRES[i] + ".txt");
            ProcessBuilder pb = new ProcessBuilder(
                    Lanzador.java(Generador.class, NOMBRES[i], String.valueOf(REGISTROS)));
            pb.redirectOutput(salidas[i]);                                         // stdout → su fichero
            pb.redirectError(new File(CARPETA, "errores_" + NOMBRES[i] + ".txt")); // stderr → otro fichero
            hijos[i] = pb.start();
            System.out.println("Padre: lanzado el hijo " + NOMBRES[i] + " (PID " + hijos[i].pid() + ")");
        }

        // 3. Esperar a cada uno (con tiempo máximo, por si alguno se cuelga) y comprobar su código
        boolean[] correcto = new boolean[NOMBRES.length];
        for (int i = 0; i < hijos.length; i++) {
            if (!hijos[i].waitFor(30, TimeUnit.SECONDS)) {
                hijos[i].destroy();
                hijos[i].waitFor();
                System.out.println("Padre: el hijo " + NOMBRES[i] + " tardaba demasiado; lo he destruido");
            }
            int codigo = hijos[i].exitValue(); // ya ha terminado: exitValue() no lanza excepción
            correcto[i] = codigo == 0;
            System.out.println("Padre: el hijo " + NOMBRES[i] + " ha terminado con código " + codigo);
        }
        long milis = System.currentTimeMillis() - inicio;

        // 4. Leer y procesar los ficheros
        System.out.println("\n===== RESUMEN =====");
        int lineasTotales = 0;
        for (int i = 0; i < NOMBRES.length; i++) {
            if (!correcto[i]) {
                System.out.println("Hijo " + NOMBRES[i] + ": falló; mira " + CARPETA + "/errores_" + NOMBRES[i] + ".txt");
                continue;
            }
            int lineas = 0;
            int suma = 0;
            int maximo = Integer.MIN_VALUE;
            try (BufferedReader lector = new BufferedReader(new FileReader(salidas[i], Lanzador.CHARSET_JAVA))) {
                String linea;
                while ((linea = lector.readLine()) != null) {
                    int valor = Integer.parseInt(linea.split(";")[2]); // «A;3;57» → 57
                    lineas++;
                    suma += valor;
                    maximo = Math.max(maximo, valor);
                }
            }
            lineasTotales += lineas;
            System.out.printf("Hijo %s: %d líneas · suma %d · media %.1f · máximo %d%n",
                    NOMBRES[i], lineas, suma, lineas == 0 ? 0.0 : (double) suma / lineas, maximo);
        }
        System.out.println("Total: " + lineasTotales + " líneas en " + milis + " ms"
                + " (en serie habrían tardado más o menos la suma de los dos)");
    }
}
