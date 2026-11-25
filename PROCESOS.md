# Chuleta de procesos · PSP UT01 (apartado 5)

Todo lo que el profesor ha dado sobre procesos, resumido y con un ejemplo que se puede ejecutar para cada
apartado. Las fuentes son el temario (5.1–5.4), las diapositivas de las sesiones 4 y 5, el tutorial
*GeneradorProcesos* de NetBeans, la guía de gestión de procesos en Windows y Linux, la relación de 4 ejercicios
sobre procesos y el proyecto *EjemploTuberias*. La ha escrito Claude el 25/11/2026 y los 7 ejemplos se han
ejecutado en Windows con JDK 25.

## Ejemplos ejecutables

Están en `src/main/java/es/juanjovera/chuletas/procesos/`. Para ejecutarlos, pulsa ▶ junto al `main` del
`Main.java` de cada carpeta. Los ficheros que generan van a `ficheros/`, en la carpeta del proyecto.

| Carpeta | Qué enseña | Material del profesor |
|---|---|---|
| `crear/` | `ProcessBuilder` escrito a mano, `Runtime.exec`, `pid()`, `inheritIO()`, un comando distinto para cada SO, `directory()` y `environment()` | Diapositivas 18–20, ejemplo *CrearProcesos* |
| `gestionar/` | `isAlive()`, `exitValue()` con el hijo vivo, `waitFor(tiempo)`, `destroy()` y los hijos de un proceso | Diapositiva 27, tutorial *GeneradorProcesos* |
| `salida/` | `getInputStream()` y `getErrorStream()`, códigos de salida, `cmd /c dir` y `redirectErrorStream` | Diapositiva 24, relación, ejercicio 1 |
| `redireccion/` | `redirectOutput`, `Redirect.appendTo`, `redirectError` (`errores.txt`), `redirectInput` y un programa que no existe | Relación, ejercicio 2 |
| `entrada/` | `getOutputStream()` para mandar datos al hijo, con `flush()` y `close()` | Relación, ejercicios 3 y 4 |
| `tuberia/` | La salida de un proceso como entrada de otro: a mano, con `startPipeline` y con el `\|` de la consola | *EjemploTuberias* |
| `paralelo/` | **Todo junto**: dos hijos en paralelo, cada uno con su fichero, espera, códigos de salida y un resumen | Esquema del ejercicio 2 de la tarea |
| `hijos/` | Los programas que lanzan los ejemplos (`Saludo`, `Generador`, `Eco`, `Nombres`, `Dormilon`, `DondeEstoy`) | *LeerNombre*, *Tuberia*, *DarNombres* |

`Lanzador.java` monta el comando `java -cp … paquete.Clase` para lanzar un hijo del propio proyecto
(en `crear/` está escrito a mano).

## Conceptos

- **Programa o ejecutable:** el fichero en disco. **Proceso:** ese programa en ejecución. El SO le da un
  **PID**, memoria propia y un **PCB** (bloque de control con su estado, registros y ficheros abiertos).
- **Proceso e hilo:** un proceso **no puede acceder a la memoria de otro** porque cada uno tiene su contexto.
  Los hilos de un mismo proceso comparten sus variables. Para pasar datos entre procesos hace falta un
  mecanismo de comunicación.
- **Ciclo de vida:** *creación* (fork/exec: PID y memoria) → *preparado/ejecución* (el planificador le da
  turnos de CPU) → *bloqueado* (espera E/S o un evento) → *terminación* (se liberan el PCB, la memoria y los
  recursos).
- **Padre e hijo:** el proceso que llama a `start()` es el padre y el nuevo es el hijo. El hijo guarda el PID
  de su padre (**PPID**). Para ver el árbol: `pstree -p` en Linux, o *Process Explorer* con Ctrl+T en Windows.
- **Huérfanos:** si el padre llama a `destroy()` sobre sus hijos, estos se cierran con él. Si matas al padre
  desde el Administrador de tareas, los hijos siguen vivos y quedan **huérfanos**: los adopta `init`/`systemd`.
- **Una JVM es un proceso:** al pulsar ▶ se crea un solo `java.exe`, pero con 15–30 hilos (`main`, recolector
  de basura, compilador JIT, finalizador…).
- **Comunicación entre procesos:** tuberías, colas de mensajes, sockets y memoria compartida. En esta unidad
  se usan **tuberías**: una cadena de procesos en la que la salida de uno es la entrada del siguiente.
- **Flujos estándar:** entrada (`stdin`, `System.in`), salida (`stdout`, `System.out`) y error (`stderr`,
  `System.err`). **Redirigir** es conectarlos a un fichero o a otro proceso en vez de a la consola.
- **Código de salida:** el número que devuelve un proceso al terminar (`System.exit(n)`). Un 0 significa que ha
  ido bien y cualquier otro valor, que ha fallado. El padre lo recoge con `waitFor()` o `exitValue()`.

## `ProcessBuilder`: preparar y lanzar

| Método | Para qué |
|---|---|
| `new ProcessBuilder("cmd", "/c", "dir")` | El comando, **una palabra en cada `String`**. También acepta una `List<String>`. |
| `command(...)` | Ver o cambiar el comando |
| `directory(new File("..."))` | Carpeta de trabajo del hijo (por defecto, la del padre) |
| `environment().put("CLAVE", "valor")` | Variables de entorno del hijo (es una copia de las del padre) |
| `redirectOutput(File)` / `redirectError(File)` | Salida estándar o de error a un fichero, que se vacía antes |
| `redirectOutput(Redirect.appendTo(File))` | Igual, pero añade al final del fichero |
| `redirectInput(File)` | El hijo lee su entrada estándar de un fichero |
| `redirectErrorStream(true)` | La salida de error va por el mismo sitio que la estándar |
| `inheritIO()` | Las tres, a la consola del padre |
| `start()` | **Crea el proceso** y devuelve un `Process`. Si el programa no existe, lanza `IOException`. |
| `ProcessBuilder.startPipeline(lista)` | Lanza varios procesos conectados en tubería (Java 9+) |

`Runtime.getRuntime().exec(String[])` es la forma antigua y también devuelve un `Process`. Puede lanzar
`IOException`, `SecurityException` (sin permiso para crear procesos), `NullPointerException` o
`IllegalArgumentException` (comando nulo o vacío). La versión `exec(String)` está obsoleta porque trocea el
comando por los espacios.

## `Process`: controlar al hijo

| Método | Para qué |
|---|---|
| `getInputStream()` | Leer la **salida estándar** del hijo. El nombre despista: es la entrada del padre. |
| `getErrorStream()` | Leer la **salida de error** del hijo |
| `getOutputStream()` | Escribir en la **entrada estándar** del hijo. Hay que terminar con `\n`, hacer `flush()` y, al acabar, `close()`. |
| `waitFor()` | Bloquea al padre hasta que el hijo termina y devuelve su código de salida. Lanza `InterruptedException`. |
| `waitFor(5, TimeUnit.SECONDS)` | Espera como mucho ese tiempo. Devuelve `true` si el hijo ha terminado. |
| `exitValue()` | Código de salida. **No espera**: si el hijo sigue vivo, lanza `IllegalThreadStateException`. |
| `isAlive()` | Si el hijo sigue en ejecución |
| `destroy()` / `destroyForcibly()` | Pide que termine (SIGTERM) / lo mata a la fuerza (SIGKILL) |
| `pid()` | PID del hijo. `ProcessHandle.current().pid()` da el del propio proceso. |

## El esquema del ejercicio 2

```java
ProcessBuilder pb1 = new ProcessBuilder(java, "-cp", classpath, "paquete.Hijo", "A");
pb1.redirectOutput(new File("salida_1.txt"));      // cada hijo, a su fichero
// … lo mismo con pb2 …
Process h1 = pb1.start();                           // lanzar LOS DOS…
Process h2 = pb2.start();
int c1 = h1.waitFor();                              // …y solo entonces esperar
int c2 = h2.waitFor();
if (c1 == 0) { /* leer salida_1.txt con BufferedReader y procesar */ }
```

Donde `java = System.getProperty("java.home") + "/bin/java"` y `classpath = System.getProperty("java.class.path")`.
Versión completa en `paralelo/Main.java`.

## Trampas: lo que hay que mirar antes de dar el código por bueno

1. **Cada argumento, un `String`.** `new ProcessBuilder("java -cp out Hijo")` busca un programa que se llame
   así, con espacios incluidos, y lanza `IOException`.
2. **Las órdenes internas y el `|` necesitan intérprete.** `dir`, `echo`, `type`, `|`, `>` no son programas:
   se lanzan con `"cmd", "/c", "orden"` en Windows o `"sh", "-c", "orden"` en Linux, con la orden entera como
   **un solo** argumento.
3. **Para lanzar un hijo Java:** el `java` de `java.home`, `-cp` con `java.class.path` y la clase **con su
   paquete**. Si cambias `directory()` y el classpath es relativo, el hijo no encuentra la clase
   («Could not find or load main class»).
4. **En paralelo, primero todos los `start()` y después los `waitFor()`.** Si alternas `start` y `waitFor`, los
   hijos se ejecutan uno detrás de otro.
5. **Si lees por tubería, lee antes de hacer `waitFor()`.** El hijo escribe en una tubería pequeña. Si nadie la
   vacía, el hijo se queda bloqueado escribiendo y el padre esperando para siempre. Con `redirectOutput` a un
   fichero no pasa: ahí se espera primero y se lee después.
6. **`exitValue()`, solo con el hijo terminado.** Si no, lanza `IllegalThreadStateException`. Usa `waitFor()`.
7. **Un programa que no existe y un comando que falla son cosas distintas.** El primero lanza `IOException` en
   `start()`. El segundo arranca y termina con un código distinto de 0.
8. **Al escribir en el hijo:** termina la línea con `\n`, haz `flush()` y, cuando acabes, `close()`. Si no
   cierras, el `readLine()` del hijo nunca devuelve `null` y no termina.
9. **Tildes rotas.** Un hijo Java escribe en el juego de caracteres del sistema (Cp1252 en Windows), y `cmd`
   escribe en IBM850. Léelos con ese juego: `new InputStreamReader(flujo, Charset.forName(System.getProperty("native.encoding")))`.
   Si lees en UTF-8 un fichero con tildes en Cp1252, `Files.readAllLines` lanza `MalformedInputException`.
10. **Las rutas relativas parten de la carpeta de trabajo del padre.** En IntelliJ y NetBeans es la carpeta del
    proyecto: `new File("salida_proceso1.txt")` queda junto al `pom.xml`.
11. **Con la salida redirigida a un fichero, `getInputStream()` no devuelve nada.** La salida ya va al fichero.
12. **`inheritIO()` primero, y los cambios después.** `inheritIO()` pone los tres flujos en la consola. Si
    después llamas a `redirectInput(f)`, solo cambia la entrada.
13. **`System.exit(-1)` no se lee igual en todos los sistemas.** En Windows llega como -1 y en Linux como 255.
    Usa códigos positivos: 0 si va bien y 1, 2, 3… para cada error.

## Herramientas gráficas (5.1)

| Qué quiero ver | Windows | Linux |
|---|---|---|
| Procesos, PID, CPU y memoria | Administrador de tareas (Ctrl+Mayús+Esc) → *Detalles*, y añadir las columnas PID, Subprocesos y Línea de comandos | Monitor del sistema (`gnome-system-monitor`) → *Procesos* |
| Árbol padre-hijo | *Process Explorer* (Sysinternals), Ctrl+T | Monitor del sistema → *Mostrar dependencias* |
| Hilos de un proceso | *Process Explorer* → doble clic → pestaña *Threads* | Propiedades del proceso |
| Ficheros abiertos | *Process Explorer* → *View* → *Lower Pane* → *Handles* | Clic derecho → *Archivos abiertos* |
| Actividad de E/S en directo | *Process Monitor* (Sysinternals) | — |
| Prioridad | Clic derecho → *Establecer prioridad* | Clic derecho → *Cambiar prioridad* (valor nice) |

## Comandos (5.2)

Para ver la ayuda de una orden: `man orden` en Linux u `orden /?` en Windows.

| Acción | Windows (CMD / PowerShell) | Linux (Bash) |
|---|---|---|
| Listar procesos | `tasklist` · `Get-Process` | `ps aux` · `ps -ef` (con PPID) |
| Filtrar | `tasklist /FI "IMAGENAME eq java.exe"` · `tasklist /FI "MEMUSAGE gt 50000"` | `ps aux \| grep java` |
| Los 10 que más RAM usan | `Get-Process \| Sort-Object WS -Descending \| Select-Object -First 10` | `ps aux --sort=-%mem \| head -n 11` |
| Los 10 que más CPU usan | `Get-Process \| Sort-Object CPU -Descending \| Select-Object -First 10` | `ps aux --sort=-%cpu \| head -n 11` |
| Árbol padre-hijo | *Process Explorer* | `pstree -p` · `pstree -AGu` |
| Monitor en tiempo real | `taskmgr` | `top` · `htop` (se sale con `q`) |
| Terminar (cierre ordenado) | `taskkill /PID 1234` | `kill 1234` (SIGTERM, 15) |
| Matar a la fuerza | `taskkill /F /PID 1234` · `Stop-Process -Id 1234 -Force` | `kill -9 1234` (SIGKILL) |
| Matar con sus hijos | `taskkill /F /T /PID 1234` | — |
| Matar por nombre | `taskkill /IM notepad.exe /F` | `killall gedit` |
| Prioridad | Administrador de tareas | `nice -n 5 programa` · `renice -n -5 -p 1234` (de -20, la más alta, a 19, la más baja; bajar el valor requiere `sudo`) |
| Guardar en un fichero | `… > informe.txt` · `… \| Out-File informe.txt` | `… > informe.txt` |

El `head -n 11` saca 11 líneas porque la primera es la cabecera.

## Preguntas típicas

- **¿Se puede lanzar cualquier aplicación desde Java?** Sí, siempre que se conozca la orden que la lanza
  desde la consola y se tenga permiso.
- **¿Se puede saber si un proceso sigue activo?** Sí, con `isAlive()`.
- **¿Quién gestiona los procesos de Java, la JVM o el SO?** El SO. Cada `start()` crea un proceso real, con
  su propio PID, que aparece en `tasklist` y en `ps`.
- **¿En qué se diferencian `waitFor()` y `exitValue()`?** `waitFor()` espera a que el hijo termine y luego
  devuelve el código. `exitValue()` lo devuelve en el momento, y si el hijo no ha terminado lanza una excepción.
- **¿Por qué `getInputStream()` lee la salida del hijo?** Porque el nombre está puesto desde el padre: es su
  flujo de *entrada*, por el que le llega lo que el hijo escribe.
- **¿Qué ventaja tiene `ProcessBuilder` sobre `Runtime.exec`?** Permite configurar el comando con sus
  argumentos, la carpeta de trabajo, el entorno y las redirecciones antes de lanzarlo. `Runtime.exec` se
  mantiene por compatibilidad.
