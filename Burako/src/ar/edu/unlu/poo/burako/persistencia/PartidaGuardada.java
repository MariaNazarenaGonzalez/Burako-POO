package ar.edu.unlu.poo.burako.persistencia;

import ar.edu.unlu.poo.burako.modelo.Burako;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
 * omisión, ya que es el propio modelo quien define cuál es su estado completo.
 *
 * Los campos propios de esta clase son solo metadatos de persistencia
 * (identificador de archivo, usuarios participantes, fecha de guardado)
 * que el modelo no tiene motivo para conocer.
 *
 * MODIFICADO (Fase 10 - Soporte 2 o 4 jugadores):
 * - Los campos fijos idUsuario1/2 y nombreUsuario1/2 se reemplazaron por
 *   listas (idsUsuarios/nombresUsuarios) para admitir tanto partidas de 2
 *   como de 4 usuarios sin duplicar la clase. Se conserva el constructor de
 *   2 usuarios (delegando a la lista) para no romper compatibilidad con
 *   código y tests existentes.
 */
public class PartidaGuardada implements Serializable {

    private static final long serialVersionUID = 2L;

    private final String id;
    private final List<String> idsUsuarios;
    private final List<String> nombresUsuarios;
    private final LocalDateTime fechaGuardado;
    private final Burako estado;

    /** Partida de 2 usuarios (comportamiento original, sin cambios). */
    public PartidaGuardada(String id, Usuario usuario1, Usuario usuario2, Burako estado) {
        this(id, List.of(usuario1, usuario2), estado);
    }

    /** Partida de 2 o 4 usuarios. NUEVO (Fase 10). */
    public PartidaGuardada(String id, List<Usuario> usuarios, Burako estado) {
        this.id = id;
        this.idsUsuarios = usuarios.stream().map(Usuario::getId).collect(Collectors.toList());
        this.nombresUsuarios = usuarios.stream().map(Usuario::getNombre).collect(Collectors.toList());
        this.fechaGuardado = LocalDateTime.now();
        this.estado = estado;
    }

    public String getId()                   { return id; }
    public LocalDateTime getFechaGuardado() { return fechaGuardado; }

    /** Ids de todos los usuarios participantes, en orden de índice de jugador. */
    public List<String> getIdsUsuarios() { return new ArrayList<>(idsUsuarios); }

    /** Nombres de todos los usuarios participantes, en orden de índice de jugador. */
    public List<String> getNombresUsuarios() { return new ArrayList<>(nombresUsuarios); }

    /** El estado exacto del modelo, listo para continuar la partida. */
    public Burako getEstado() { return estado; }

    public boolean participaUsuario(String idUsuario) {
        return idsUsuarios.contains(idUsuario);
    }

    @Override
    public String toString() {
        return id + " | " + String.join(" vs ", nombresUsuarios) + " | " + fechaGuardado;
    }
}
