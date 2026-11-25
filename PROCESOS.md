# Chuleta de procesos · PSP UT01 (apartado 5)

Un solo ejercicio que usa todo lo que el profesor ha dado de procesos, con `ProcessBuilder`. Está en
`src/main/java/es/juanjovera/chuletas/procesos/`. Se ejecuta con ▶ junto al `main` de `Main.java` y los ficheros
quedan en `ficheros/`, en la carpeta del proyecto.

## El ejercicio: una estación meteorológica

| Clase | Papel |
|---|---|
| `Main` | **Padre**. Coordina todo. |
| `Sensor` | **Hijo**. Escribe N temperaturas con un retardo aleatorio y termina con un código de salida. |
| `Calculadora` | **Hijo**. Lee temperaturas de su entrada estándar y contesta con la media y la máxima. |

| Paso del `Main` | Qué usa |
|---|---|
| 1. Lanza 3 sensores en paralelo, cada uno con su salida y sus errores en ficheros. El sensor C falla a propósito. | `new ProcessBuilder(...)`, `redirectOutput`, `redirectError`, `start`, `pid`, `isAlive` |
| 2. Comprueba con `tasklist` / `ps` que los hijos son procesos del SO. | Un comando del sistema e `inheritIO` |
| 3. Espera a cada hijo y mira su código de salida. | `waitFor(10, SECONDS)`, `destroy`, `exitValue` |
| 4. Lee los ficheros de los que han ido bien y hace un resumen. | `BufferedReader` sobre el fichero |
| 5. Manda las temperaturas a la calculadora y lee su respuesta. | `getOutputStream`, `getInputStream`, `Redirect.INHERIT`, `waitFor` |

Los pasos 1, 3 y 4 son el esquema del ejercicio 2 de la tarea.

## `ProcessBuilder`: preparar y lanzar

| Método | Para qué |
|---|---|
| `new ProcessBuilder(lista)` o `new ProcessBuilder("cmd", "/c", "dir")` | El comando, **una palabra en cada `String`** |
| `redirectOutput(File)` / `redirectError(File)` | Salida estándar o de error del hijo a un fichero, que se vacía antes |
| `redirectOutput(Redirect.appendTo(File))` | Igual, pero añadiendo al final |
| `redirectInput(File)` | El hijo lee su entrada de un fichero |
| `redirectError(Redirect.INHERIT)` / `inheritIO()` | Un flujo o los tres, a la consola del padre |
| `redirectErrorStream(true)` | Los errores van por el mismo sitio que la salida |
| `directory(File)` / `environment().put(k, v)` | Carpeta de trabajo y variables de entorno del hijo |
| `start()` | **Crea el proceso** y devuelve un `Process`. Si el programa no existe, lanza `IOException`. |

## `Process`: controlar al hijo

| Método | Para qué |
|---|---|
| `waitFor()` | Espera a que el hijo termine y devuelve su **código de salida** (0 = bien) |
| `waitFor(10, TimeUnit.SECONDS)` | Espera como mucho ese tiempo. Devuelve `false` si el hijo sigue vivo. |
| `exitValue()` | El código de salida, **sin esperar**. Si el hijo vive, lanza `IllegalThreadStateException`. |
| `isAlive()` · `pid()` · `destroy()` | ¿Sigue vivo? · Su PID · Pedirle que termine |
| `getInputStream()` | Leer la **salida** del hijo (es la entrada del padre) |
| `getErrorStream()` | Leer los **errores** del hijo |
| `getOutputStream()` | Escribir en la **entrada** del hijo. Al acabar, `close()`. |

Para lanzar como hijo una clase Java del propio proyecto:
`System.getProperty("java.home") + "/bin/java"`, `"-cp"`, `System.getProperty("java.class.path")`,
`"paquete.Clase"` y sus argumentos (método `comandoJava` del `Main`).

## Trampas

1. **Cada palabra, un `String`.** `new ProcessBuilder("java -version")` lanza `IOException`.
2. **`dir`, `echo`, `|` y `>` no son programas.** Se lanzan con `"cmd", "/c", "orden"` en Windows o con
   `"sh", "-c", "orden"` en Linux.
3. **Para que vayan en paralelo, primero todos los `start()` y después los `waitFor()`.** Si los alternas, los
   hijos se ejecutan uno detrás de otro.
4. **Si lees del hijo por tubería, lee antes de `waitFor()`.** Si no, la tubería se llena y se bloquean los dos.
   Con la salida en un fichero no pasa: se espera primero y se lee después.
5. **`exitValue()`, solo con el hijo terminado.**
6. **Al escribir en el hijo, cierra el flujo.** Hasta que no cierras, su `readLine()` no devuelve `null` y no termina.
7. **Un programa que no existe no es lo mismo que uno que falla.** El primero lanza `IOException` en `start()`.
   El segundo arranca y termina con un código distinto de 0.
8. **Las rutas relativas parten de la carpeta de trabajo**, que en IntelliJ y NetBeans es la del proyecto.
9. **Usa códigos de salida positivos.** `System.exit(-1)` llega como -1 en Windows y como 255 en Linux.
10. **Tildes:** un hijo Java escribe en Cp1252 en Windows y `cmd`, en IBM850. Si los lees en UTF-8, las tildes
    salen rotas. Por eso los hijos de este ejercicio no escriben tildes.

## Conceptos

- **Proceso:** un programa en ejecución, con su PID, su memoria y su PCB. Un proceso no ve la memoria de otro.
  Los hilos de un mismo proceso sí comparten la suya.
- **Ciclo de vida:** creación → preparado/ejecución → bloqueado (espera E/S) → terminación.
- **Padre e hijo:** el que llama a `start()` es el padre. Si el padre muere a la fuerza, sus hijos siguen vivos
  y quedan **huérfanos**, adoptados por `init`/`systemd`.
- **Comunicación entre procesos:** tuberías, colas, sockets y memoria compartida. En la UT, **tuberías**: la
  salida de uno es la entrada de otro.
- **Una JVM es un proceso** (`java.exe`) con muchos hilos: main, recolector de basura, compilador JIT…

## Comandos del sistema operativo

| Acción | Windows | Linux |
|---|---|---|
| Listar procesos | `tasklist` · `Get-Process` | `ps aux` · `ps -ef` (con el PPID) |
| Filtrar | `tasklist /FI "IMAGENAME eq java.exe"` | `ps aux \| grep java` |
| Los 10 que más RAM usan | `Get-Process \| Sort-Object WS -Descending \| Select-Object -First 10` | `ps aux --sort=-%mem \| head -n 11` |
| Los 10 que más CPU usan | `Get-Process \| Sort-Object CPU -Descending \| Select-Object -First 10` | `ps aux --sort=-%cpu \| head -n 11` |
| Árbol padre-hijo | *Process Explorer* (Ctrl+T) | `pstree -p` |
| Terminar / matar | `taskkill /PID 1234` · `taskkill /F /PID 1234` | `kill 1234` · `kill -9 1234` |
| Matar por nombre | `taskkill /IM notepad.exe /F` | `killall gedit` |
| Prioridad | Administrador de tareas | `nice -n 5 programa` · `renice -n -5 -p 1234` |

Herramientas gráficas: el Administrador de tareas (pestaña *Detalles*) y *Process Explorer* en Windows, y el
Monitor del sistema en Linux.
