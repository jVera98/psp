package es.juanjovera.chuletas.procesos.hijos;

/**
 * HIJO. Saluda al nombre que recibe en los argumentos del main y termina con un código de salida.
 * Es el «LeerNombre» del ejercicio 1 de la relación del profesor.
 *
 * Código de salida (lo que el padre recoge con waitFor() o exitValue()):
 * - 0  → todo bien. Es el que devuelve cualquier programa que termina el main sin llamar a System.exit.
 * - ≠0 → algo ha ido mal. El número lo eliges tú y le dice al padre QUÉ ha fallado.
 *
 * La relación del profesor usa System.exit(1) para «bien» y System.exit(-1) para «mal». Funciona, pero lo
 * habitual es 0 = bien. Además, -1 no llega igual a todos los sistemas: Windows lo devuelve como -1 y Linux
 * como 255 (el código de salida es un byte sin signo).
 */
public class Saludo {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Saludo: falta el nombre. Uso: Saludo <nombre>"); // a la salida de ERROR
            System.exit(2);
        }
        System.out.println("Hola " + args[0]); // a la salida ESTÁNDAR
        System.exit(0);
    }
}
