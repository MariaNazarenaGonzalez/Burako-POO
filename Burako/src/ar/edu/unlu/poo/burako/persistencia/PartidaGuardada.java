package ar.edu.unlu.poo.burako.persistencia;

import ar.edu.unlu.poo.burako.modelo.Burako;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Snapshot persistible del estado completo de una partida.
 *
 * Envuelve directamente la instancia concreta de {@link Burako}: al ser el
 * agregado raíz del modelo, referencia transitivamente todo lo necesario
 * para continuar la partida exactamente donde quedó (jugadores, atriles,
 * mazo, pozo, juegos bajados, muertos y turno actual).
 *
 * DECISIÓN DE DISEÑO: se serializa el objeto de dominio existente tal cual,
 * en lugar de copiar sus datos a un DTO paralelo. Esto evita duplicar la
 * estructura del modelo en la capa de persistencia (lo que violaría DRY y
 * obligaría a mantener dos representaciones sincronizadas) y garantiza que
 * ningún dato necesario para continuar la partida quede afuera por
 * omisión, ya que es el propio modelo (fases 1 a 5, ya estabilizado) quien
 * define cuál es su estado completo.
 *
 * Los campos propios de esta clase son solo metadatos de persistencia
 * (identificador de archivo, usuarios participantes, fecha de guardado)
 * que el modelo no tiene motivo para conocer.
 */
public class PartidaGuardada implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String idUsuario1;
    private final String idUsuario2;
    private final String nombreUsuario1;
    private final String nombreUsuario2;
    private final LocalDateTime fechaGuardado;
    private final Burako estado;

    public PartidaGuardada(String id, Usuario usuario1, Usuario usuario2, Burako estado) {
        this.id = id;
        this.idUsuario1 = usuario1.getId();
        this.idUsuario2 = usuario2.getId();
        this.nombreUsuario1 = usuario1.getNombre();
        this.nombreUsuario2 = usuario2.getNombre();
        this.fechaGuardado = LocalDateTime.now();
        this.estado = estado;
    }

    public String getId()                   { return id; }
    public String getIdUsuario1()           { return idUsuario1; }
    public String getIdUsuario2()           { return idUsuario2; }
    public String getNombreUsuario1()       { return nombreUsuario1; }
    public String getNombreUsuario2()       { return nombreUsuario2; }
    public LocalDateTime getFechaGuardado() { return fechaGuardado; }

    /** El estado exacto del modelo, listo para continuar la partida. */
    public Burako getEstado() { return estado; }

    public boolean participaUsuario(String idUsuario) {
        return idUsuario1.equals(idUsuario) || idUsuario2.equals(idUsuario);
    }

    @Override
    public String toString() {
        return id + " | " + nombreUsuario1 + " vs " + nombreUsuario2 + " | " + fechaGuardado;
    }
}
