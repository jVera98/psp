# Chuletas para el examen · PSP UT01 (procesos, hilos y sincronización)

Todo está en `src/main/java/es/juanjovera/chuletas/`. **Abrir:** IntelliJ → *Open* → esta carpeta (el `pom.xml`).
**Ejecutar:** botón ▶ junto al `main` del `Main.java` de cada paquete (JDK 25).

## Procesos (apartado 5)

La chuleta completa está en **[PROCESOS.md](PROCESOS.md)**: conceptos, `ProcessBuilder` y `Process` método a
método, el esquema del ejercicio 2, las trampas típicas y los comandos de Windows y Linux. Cada apartado tiene un
ejemplo ejecutable en `procesos/`:

| Carpeta | Qué enseña |
|---|---|
| `procesos/crear/` | Lanzar un proceso: `ProcessBuilder`, `Runtime.exec`, comando según el SO, `directory`, `environment` |
| `procesos/gestionar/` | `isAlive`, `waitFor` con tiempo máximo, `exitValue`, `destroy` |
| `procesos/salida/` | Leer lo que escribe el hijo y su código de salida |
| `procesos/redireccion/` | Salida, errores y entrada desde y hacia ficheros |
| `procesos/entrada/` | Mandar datos al hijo con `getOutputStream` |
| `procesos/tuberia/` | La salida de un proceso como entrada de otro |
| `procesos/paralelo/` | Todo junto: dos hijos en paralelo con su fichero cada uno y un resumen (como el ejercicio 2) |

## Hilos y sincronización

Tres problemas clásicos, cada uno resuelto de dos formas: con **monitor** (`synchronized` + `wait` + `notifyAll`)
y con **semáforos** (`java.util.concurrent.Semaphore`). Los ha escrito Claude como guía de consulta (25/11/2026).
Se han ejecutado 4 veces cada uno: todos terminan solos y cumplen las reglas del problema.

| Problema | Monitor | Semáforos |
|---|---|---|
| Jardín con aforo (12 visitantes, 5 plazas) | `jardin/sync/` | `jardin/semaforo/` |
| Barbero dormido (1 barbero, 4 sillas, 10 clientes) | `barbero/sync/` | `barbero/semaforo/` |
| Cena de los filósofos (5 filósofos, 3 comidas) | `filosofos/sync/` | `filosofos/semaforo/` |

En cada carpeta, lo importante está en el **recurso compartido** (`Jardin`, `Barberia`, `Mesa`): los hilos
(`Visitante`, `Cliente`, `Barbero`, `Filosofo`) solo llaman a sus métodos. `Main` y la clase del hilo son iguales
en las dos versiones; lo único que cambia es el recurso compartido.

### Lo mismo, de las dos formas

| Quiero… | Monitor | Semáforos |
|---|---|---|
| Que solo un hilo toque los datos | método `synchronized` | `mutex = new Semaphore(1)`; `acquire()` antes del `try`, `release()` en el `finally` |
| Contar plazas libres | un `int` + `while (lleno) wait();` | `aforo = new Semaphore(N)`; `acquire()` / `release()` |
| Mirar si hay sitio sin bloquearme | `if (dentro == MAX)` dentro del `synchronized` | `if (!aforo.tryAcquire())` (mira y coge en un paso) |
| Esperar a que pase algo | `while (!condición) wait();` | semáforo de señal `new Semaphore(0)`: uno hace `acquire()`, el otro `release()` |
| Avisar de que ha pasado | `notifyAll();` después de cambiar el dato | `release()` del semáforo de señal |
| Despertar a uno en concreto | un campo en ese objeto + `notifyAll()` (cada uno mira el suyo) | un `Semaphore(0)` dentro de ese objeto (`Cliente.pelado`) |
| Terminar un hilo con `while (true)` | `join()` de los demás, luego `hilo.interrupt()` y `hilo.join()` | igual |

### Lo que hay que mirar antes de dar el código por bueno

1. **Comprobar y actuar en un solo paso.** Mirar si hay sitio y cogerlo van dentro del mismo `synchronized` o del
   mismo `mutex`, o se hacen con `tryAcquire()`. Si los separas, otro hilo se cuela en medio.
2. **No dormirse con el cerrojo cogido.** Con semáforos, los `acquire()` que esperan (aforo, `hayClientes`) van
   **fuera** del `mutex`. (`wait()` sí puede ir dentro del `synchronized`, porque suelta el cerrojo él solo.)
3. **`while`, no `if`, alrededor de `wait()`.** Al despertar, otro pudo adelantarse.
4. **`notifyAll()`, no `notify()`.** `notify()` despierta a uno cualquiera, que puede no ser el que debía.
5. **Sin espera activa.** Nada de `while (!tryAcquire()) { System.out.println(...) }`: el hilo tiene que quedarse
   bloqueado en `acquire()` o en `wait()`, y el mensaje de espera tiene que salir una sola vez.
6. **El mensaje, antes de soltar.** Escribe «Sale» o «Suelta» antes del `release()` o del `notifyAll()`. Así, el
   «Entra» o «Coge» del otro hilo nunca aparece antes en la consola.
7. **Las trazas, exactas.** Copia el texto del enunciado con su punto final y sus paréntesis.
8. **El `main` termina el último.** `join()` de todos los hilos y el mensaje de «Fin» después.

### Interbloqueo en los filósofos (la pregunta típica)

Si todos cogen primero el tenedor izquierdo, los 5 pueden quedarse con uno y esperar el otro para siempre. Hay
tres formas de evitarlo, y cada una rompe una de las condiciones del interbloqueo:

- **Coger los dos a la vez o ninguno** (`filosofos/sync`): nadie retiene un tenedor mientras espera el otro.
- **Ordenar los recursos** (`filosofos/semaforo`): todos cogen primero el tenedor de número menor, y así ya no
  puede formarse la espera en círculo.
- **Dejar como mucho N−1 sentados** con un `Semaphore(N - 1)`: siempre queda alguien que puede comer.

> **Ojo con la tarea online 1:** el barbero de aquí manda directo a la silla de corte al cliente que despierta al
> barbero, sin pasar por la sala. El 24/11 decidiste dejarlo como lo tienes en la tarea. Esto es una chuleta para
> el examen, no para copiar en la entrega.
