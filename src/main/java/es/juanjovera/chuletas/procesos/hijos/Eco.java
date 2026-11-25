package es.juanjovera.chuletas.procesos.hijos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * HIJO. Lee líneas de su ENTRADA estándar y saluda a cada una, hasta leer «Fin» o hasta que se acabe la
 * entrada (readLine() devuelve null). Es el «Tuberia» / «EjemploLectura» del profesor.
 *
 * No sabe de dónde le llegan las líneas: del teclado, de un fichero (redirectInput), del padre
 * (getOutputStream) o de otro proceso (tubería). Para él siempre es System.in.
 */
public class Eco {
    public static void main(String[] args) {
        BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in));
        int leidas = 0;
        try {
            String linea = entrada.readLine();
            while (linea != null && !linea.equals("Fin")) {
                System.out.println("Hola " + linea);
                leidas++;
                linea = entrada.readLine();
            }
        } catch (IOException e) {
            System.err.println("Eco: error de E/S al leer la entrada");
            System.exit(1);
        }
        System.out.println("(Eco ha leido " + leidas + " lineas)");
    }
}
