package es.juanjovera.chuletas.procesos.hijos;

/**
 * HIJO. Escribe N nombres, uno por línea. Es el «DarNombres» del profesor, pero recibe N por argumento
 * en vez de pedirlo por teclado. Sirve de primer eslabón de una tubería: Nombres | Eco.
 */
public class Nombres {
    private static final String[] NOMBRES = {"Fran", "Antonio", "Pepe", "Amalia", "Elena", "Ana", "Diego"};

    public static void main(String[] args) {
        int cuantos = args.length == 1 ? Integer.parseInt(args[0]) : 3;
        for (int i = 0; i < cuantos; i++) {
            System.out.println(NOMBRES[i % NOMBRES.length]);
        }
    }
}
