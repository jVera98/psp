package es.juanjovera.chuletas.procesos;

import java.util.Random;

/**
 * HIJO 1. Uso: Sensor <nombre> <lecturas>
 * Escribe una lectura por línea («A;3;27» = sensor A, lectura 3, 27 grados) con un retardo aleatorio.
 *
 * Código de salida (lo recoge el padre con waitFor() o exitValue()):
 *   0 = bien · 2 = faltan argumentos · 3 = el número de lecturas no es un número
 */
public class Sensor {
    public static void main(String[] args) throws InterruptedException {
        if (args.length != 2) {
            System.err.println("Sensor: uso Sensor <nombre> <lecturas>"); // System.err = salida de ERROR
            System.exit(2);
        }
        int lecturas = 0;
        try {
            lecturas = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Sensor " + args[0] + ": '" + args[1] + "' no es un numero");
            System.exit(3);
        }

        Random azar = new Random();
        for (int i = 1; i <= lecturas; i++) {
            System.out.println(args[0] + ";" + i + ";" + azar.nextInt(15, 36)); // System.out = salida ESTÁNDAR
            Thread.sleep(azar.nextInt(100, 401));                               // retardo de 100 a 400 ms
        }
        // terminar el main sin System.exit equivale a System.exit(0)
    }
}
