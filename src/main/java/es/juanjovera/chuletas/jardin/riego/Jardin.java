package es.juanjovera.chuletas.jardin.riego;

import java.util.concurrent.Semaphore;

/**
 * Recurso compartido: el jardín con jardinero. Versión con SEMÁFOROS (java.util.concurrent.Semaphore).
 *
 * Tres semáforos, cada uno con un trabajo:
 * - aforo (5 permisos) = plazas libres. Para regar, el jardinero coge LAS 5: así nadie puede entrar aunque haya
 *   sitio, y los que llegan se quedan en aforo.acquire() hasta que las devuelve.
 * - mutex (1 permiso) = cerrojo para los datos compartidos: dentro, jardineroEsperando y regando.
 * - avisoJardinero (0 permisos) = semáforo de SEÑAL «el jardín se ha quedado vacío». El jardinero hace
 *   acquire() y se duerme; el último visitante que sale hace release() y lo despierta.
 *
 * Un semáforo no dice si alguien está esperando ni por qué: para eso están los boolean, que se leen y se
 * cambian siempre con el mutex cogido.
 */
public class Jardin {
    private static final int MAX_PLAZAS = 5;
    private final Semaphore aforo = new Semaphore(MAX_PLAZAS);
    private final Semaphore mutex = new Semaphore(1);
    private final Semaphore avisoJardinero = new Semaphore(0);

    private int dentro = 0;
    private boolean jardineroEsperando = false; // el jardinero quiere regar y espera a que se vacíe
    private boolean regando = false;            // solo para elegir la traza del visitante que espera

    public void entrar(Visitante v) throws InterruptedException {
        System.out.println("Visitante " + v.getId() + ": Llega a la puerta.");
        if (!aforo.tryAcquire()) {
            // No hay plaza: o está lleno o el jardinero tiene las 5. El boolean dice cuál de las dos.
            boolean hayRiego;
            mutex.acquire();
            try {
                hayRiego = regando;
            } finally {
                mutex.release();
            }
            if (hayRiego) {
                System.out.println("Visitante " + v.getId() + ": Espera en la puerta (riego en curso).");
            } else {
                System.out.println("Visitante " + v.getId() + ": Espera en la puerta (jardín lleno).");
            }
            aforo.acquire(); // se duerme aquí, FUERA del mutex
        }
        mutex.acquire();
        try {
            dentro++;
            System.out.println("Visitante " + v.getId() + ": Entra (dentro: " + dentro + "/" + MAX_PLAZAS + ").");
        } finally {
            mutex.release();
        }
    }

    public void salir(Visitante v) throws InterruptedException {
        mutex.acquire();
        try {
            dentro--;
            System.out.println("Visitante " + v.getId() + ": Sale (dentro: " + dentro + "/" + MAX_PLAZAS + ").");
            aforo.release();
            // Avisa SOLO si es el último Y el jardinero espera. Si avisara siempre que se vacía, los permisos de
            // avisoJardinero se acumularían y en el siguiente riego el jardinero pasaría con gente dentro.
            if (dentro == 0 && jardineroEsperando) {
                jardineroEsperando = false; // quien avisa lo baja: así se avisa una sola vez
                avisoJardinero.release();
            }
        } finally {
            mutex.release();
        }
    }

    public void regar() throws InterruptedException {
        // 1. Mirar si hay gente y apuntarse como «esperando», en un solo paso (con el mutex).
        boolean hayQueEsperar;
        mutex.acquire();
        try {
            hayQueEsperar = dentro > 0;
            if (hayQueEsperar) {
                jardineroEsperando = true;
                System.out.println("Jardinero: Quiere regar, espera a que salgan (dentro: " + dentro + "/" + MAX_PLAZAS + ").");
            }
        } finally {
            mutex.release();
        }

        // 2. Esperar el aviso FUERA del mutex (con él cogido nadie podría salir: interbloqueo) y solo si había
        //    gente (si ya estaba vacío, nadie va a avisar). No se pierde el aviso aunque el último salga antes de
        //    llegar aquí: el semáforo guarda el permiso. Esa es la ventaja frente a wait().
        if (hayQueEsperar) {
            avisoJardinero.acquire();
        }

        // 3. Cerrar la puerta ANTES de escribir «Empieza». acquire(MAX_PLAZAS) espera a que estén libres las 5:
        //    si alguien se ha colado justo después del aviso, espera también a que salga.
        aforo.acquire(MAX_PLAZAS);
        mutex.acquire();
        try {
            regando = true;
        } finally {
            mutex.release();
        }
        System.out.println("Jardinero: Empieza a regar.");

        Thread.sleep(1500);

        mutex.acquire();
        try {
            regando = false;
        } finally {
            mutex.release();
        }
        System.out.println("Jardinero: Termina de regar.");
        aforo.release(MAX_PLAZAS); // abre la puerta: los que esperaban entran
    }
}
