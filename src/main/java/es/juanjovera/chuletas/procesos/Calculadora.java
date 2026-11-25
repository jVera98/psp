package es.juanjovera.chuletas.procesos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * HIJO 2. Lee temperaturas de su ENTRADA estándar (una por línea) hasta que se acaba la entrada,
 * y escribe en su salida cuántas ha recibido, la media y la máxima.
 *
 * No sabe quién le escribe: puede ser el teclado o, como aquí, el padre por getOutputStream().
 * Código de salida: 0 = bien · 4 = le ha llegado algo que no es un número
 */
public class Calculadora {
    public static void main(String[] args) throws IOException {
        BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in));
        int cuantas = 0;
        int suma = 0;
        int maxima = Integer.MIN_VALUE;

        String linea;
        while ((linea = entrada.readLine()) != null) { // null = el padre ha cerrado la entrada
            try {
                int temperatura = Integer.parseInt(linea.strip());
                cuantas++;
                suma += temperatura;
                maxima = Math.max(maxima, temperatura);
            } catch (NumberFormatException e) {
                System.err.println("Calculadora: '" + linea + "' no es un numero");
                System.exit(4);
            }
        }

        System.out.println("Lecturas recibidas: " + cuantas);
        if (cuantas > 0) {
            System.out.printf("Media: %.1f grados%n", (double) suma / cuantas);
            System.out.println("Maxima: " + maxima + " grados");
        }
    }
}
