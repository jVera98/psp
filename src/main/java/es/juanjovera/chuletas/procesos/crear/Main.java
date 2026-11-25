package es.juanjovera.chuletas.procesos.crear;

import es.juanjovera.chuletas.procesos.Lanzador;
import es.juanjovera.chuletas.procesos.hijos.DondeEstoy;
import es.juanjovera.chuletas.procesos.hijos.Saludo;

import java.io.File;
import java.io.IOException;

/**
 * 5.3 CREAR PROCESOS: ProcessBuilder (lo recomendado) y Runtime.exec (lo antiguo).
 *
 * - ProcessBuilder prepara el comando; start() crea el proceso y devuelve un Process para controlarlo.
 * - Cada palabra del comando va en un String APARTE: ("java", "-cp", ruta, "Clase", "Ana"), nunca "java -cp ...".
 * - Un ProcessBuilder se puede reutilizar: cada start() lanza un proceso nuevo con la misma configuración.
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Padre: soy el PID " + ProcessHandle.current().pid());

        // 1) ProcessBuilder, escrito a mano: el mismo Java del padre, su classpath y la clase con su paquete
        System.out.println("\n--- 1) ProcessBuilder ---");
        ProcessBuilder pb = new ProcessBuilder(
                System.getProperty("java.home") + File.separator + "bin" + File.separator + "java",
                "-cp", System.getProperty("java.class.path"),
                "es.juanjovera.chuletas.procesos.hijos.Saludo", "Ana");
        pb.inheritIO();               // el hijo escribe en la MISMA consola que el padre (si no, hay que leerle)
        Process hijo = pb.start();    // aquí nace el proceso: el SO le da un PID y memoria propia
        System.out.println("Padre: he lanzado al hijo PID " + hijo.pid() + " (¿vivo? " + hijo.isAlive() + ")");
        int codigo = hijo.waitFor();  // el padre se BLOQUEA hasta que el hijo termina; devuelve su código
        System.out.println("Padre: el hijo ha terminado con código " + codigo + " (¿vivo? " + hijo.isAlive() + ")");

        // 2) Runtime.exec: la forma antigua. Pásale el comando como String[]: exec(String) está obsoleto
        //    porque trocea el texto por los espacios y falla con rutas que los tienen.
        System.out.println("\n--- 2) Runtime.getRuntime().exec ---");
        String[] comando = Lanzador.java(Saludo.class, "Luis").toArray(new String[0]);
        Process otro = Runtime.getRuntime().exec(comando);
        otro.waitFor();
        System.out.println("Padre: Runtime.exec -> código " + otro.exitValue()
                + " (su «Hola Luis» no sale: exec no tiene inheritIO y nadie ha leído su salida)");

        // 3) Un comando del sistema que cambia según el SO (la «mejora» del ejemplo CrearProcesos del temario)
        System.out.println("\n--- 3) Comando del sistema operativo ---");
        ProcessBuilder listar = Lanzador.WINDOWS
                ? new ProcessBuilder("tasklist", "/FI", "IMAGENAME eq java.exe")   // procesos java.exe
                : new ProcessBuilder("ps", "-C", "java", "-o", "pid,ppid,rss,cmd"); // ídem en Linux
        listar.inheritIO();
        System.out.println("Padre: el comando termina con código " + listar.start().waitFor());

        // 4) directory() y environment(): dónde trabaja el hijo y qué variables de entorno ve
        System.out.println("\n--- 4) directory() y environment() ---");
        File carpeta = new File(System.getProperty("java.io.tmpdir"));
        ProcessBuilder conEntorno = new ProcessBuilder(Lanzador.java(DondeEstoy.class));
        conEntorno.directory(carpeta);                  // carpeta de trabajo del hijo (por defecto, la del padre)
        conEntorno.environment().put("CURSO", "2DAM");  // copia del entorno del padre + lo que añadas
        conEntorno.inheritIO();
        conEntorno.start().waitFor();
        System.out.println("Padre: yo sigo en " + System.getProperty("user.dir")
                + " y mi CURSO = " + System.getenv("CURSO") + " (cambiar el del hijo no toca el mío)");
    }
}
