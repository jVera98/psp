package es.juanjovera.chuletas.procesos.hijos;

import java.util.Random;

/**
 * HIJO. Escribe N registros con una pausa aleatoria entre uno y otro (para ver dos hijos en paralelo).
 * Uso: Generador <nombre> <registros>
 *
 * Cada línea es un registro «nombre;número;valor», p. ej. «A;3;57»: así el padre puede trocearla con split(";").
 * Códigos de salida: 0 bien · 2 faltan argumentos · 3 el número de registros no es un número válido.
 */
public class Generador {
    public static void main(String[] args) throws InterruptedException {
        if (args.length != 2) {
            System.err.println("Generador: uso Generador <nombre> <registros>");
            System.exit(2);
        }
        String nombre = args[0];
        int registros = 0;
        try {
            registros = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Generador: " + args[1] + " no es un numero");
            System.exit(3);
        }

        Random azar = new Random();
        for (int i = 1; i <= registros; i++) {
            System.out.println(nombre + ";" + i + ";" + azar.nextInt(100));
            Thread.sleep(azar.nextInt(100, 401)); // retardo aleatorio de 100 a 400 ms
        }
    }
}
