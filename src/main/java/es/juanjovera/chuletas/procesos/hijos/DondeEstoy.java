package es.juanjovera.chuletas.procesos.hijos;

/**
 * HIJO. Dice en qué carpeta trabaja, quién es su padre y qué vale la variable de entorno CURSO.
 * Sirve para ver directory() y environment() de ProcessBuilder.
 */
public class DondeEstoy {
    public static void main(String[] args) {
        ProcessHandle yo = ProcessHandle.current();
        System.out.println("  Soy el PID " + yo.pid()
                + "; mi padre es el PID " + yo.parent().map(p -> String.valueOf(p.pid())).orElse("?"));
        System.out.println("  Trabajo en la carpeta: " + System.getProperty("user.dir"));
        System.out.println("  Variable de entorno CURSO = " + System.getenv("CURSO"));
    }
}
