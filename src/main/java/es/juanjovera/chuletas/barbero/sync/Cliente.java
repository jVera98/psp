package es.juanjovera.chuletas.barbero.sync;

/**
 * Cada cliente es un hilo: llega; si hay sitio se queda hasta que lo pelan; si no, se marcha.
 */
public class Cliente implements Runnable {
    private final int id;
    private final Barberia barberia;
    // ¿Ya lo han pelado? Solo se lee y se cambia desde métodos synchronized de Barberia (mismo cerrojo),
    // así que no hay carrera.
    private boolean pelado = false;

    public Cliente(int id, Barberia barberia) {
        this.id = id;
        this.barberia = barberia;
    }

    public int getId() {
        return id;
    }

    boolean isPelado() {
        return pelado;
    }

    void setPelado() {
        pelado = true;
    }

    @Override
    public void run() {
        try {
            if (barberia.entrar(this)) {     // false = sala llena, se ha marchado
                barberia.esperarCorte(this); // se queda hasta que el barbero termine con él
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
