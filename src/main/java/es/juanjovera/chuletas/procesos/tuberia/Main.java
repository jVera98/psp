package es.juanjovera.chuletas.procesos.tuberia;

import es.juanjovera.chuletas.procesos.Lanzador;
import es.juanjovera.chuletas.procesos.hijos.Eco;
import es.juanjovera.chuletas.procesos.hijos.Nombres;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * 5.4 TUBERÍAS: una cadena de procesos en la que la SALIDA de uno es la ENTRADA del siguiente.
 * Es lo que hace el | de la consola: «DarNombres | Tuberia» en el ejemplo EjemploTuberias del profesor.
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        // 1) A mano: el padre copia la salida del primero en la entrada del segundo
        System.out.println("--- 1) Tubería a mano: Nombres 4 | Eco ---");
        Process nombres = new ProcessBuilder(Lanzador.java(Nombres.class, "4")).start();
        Process eco = new ProcessBuilder(Lanzador.java(Eco.class)).inheritIO()
                .redirectInput(ProcessBuilder.Redirect.PIPE) // su entrada, desde el padre (no desde la consola)
                .start();
        try (InputStream salidaNombres = nombres.getInputStream();
             OutputStream entradaEco = eco.getOutputStream()) {
            salidaNombres.transferTo(entradaEco); // copia todo hasta que Nombres termina
        } // cerrar la entrada de Eco = fin de datos: su readLine() devuelve null
        System.out.println("Padre: códigos " + nombres.waitFor() + " y " + eco.waitFor());

        // 2) startPipeline (Java 9+): Java conecta las tuberías por ti
        System.out.println("\n--- 2) ProcessBuilder.startPipeline ---");
        List<Process> cadena = ProcessBuilder.startPipeline(List.of(
                new ProcessBuilder(Lanzador.java(Nombres.class, "2")),
                new ProcessBuilder(Lanzador.java(Eco.class)).redirectOutput(ProcessBuilder.Redirect.INHERIT)));
        for (Process p : cadena) {
            p.waitFor();
        }

        // 3) El | de la consola: ProcessBuilder no entiende "|", así que hay que pedírselo al intérprete
        //    de comandos (cmd /c en Windows, sh -c en Linux) y pasarle la orden entera como UN argumento.
        System.out.println("\n--- 3) Tubería del sistema operativo ---");
        ProcessBuilder consola = Lanzador.WINDOWS
                ? new ProcessBuilder("cmd", "/c", "tasklist | findstr /i java")
                : new ProcessBuilder("sh", "-c", "ps aux | grep java");
        consola.inheritIO();
        System.out.println("Padre: código " + consola.start().waitFor());
    }
}
