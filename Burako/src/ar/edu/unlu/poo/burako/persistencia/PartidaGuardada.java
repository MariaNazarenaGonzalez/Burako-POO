package ar.edu.unlu.poo.burako.persistencia;

import ar.edu.unlu.poo.burako.modelo.Burako;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Representa una partida almacenada para poder reanudarla posteriormente.
 *
 * Contiene el estado completo de la partida junto con la información de los
 * jugadores participantes y los metadatos asociados al momento en que fue
 * guardada.
 */
public class PartidaGuardada implements Serializable {

    private static final long serialVersionUID = 2L;

    private final String id;
    private final List<String> idsUsuarios;
    private final List<String> nombresUsuarios;
    private final LocalDateTime fechaGuardado;
    private final Burako estado;

    /**
     * Crea una partida guardada a partir de dos usuarios participantes.
     */
    public PartidaGuardada(String id, Usuario usuario1, Usuario usuario2, Burako estado) {
        this(id, List.of(usuario1, usuario2), estado);
    }

    /**
     * Crea una partida guardada utilizando la lista de usuarios participantes.
     */
    public PartidaGuardada(String id, List<Usuario> usuarios, Burako estado) {
        this.id = id;
        this.idsUsuarios = usuarios.stream().map(Usuario::getId).collect(Collectors.toList());
        this.nombresUsuarios = usuarios.stream().map(Usuario::getNombre).collect(Collectors.toList());
        this.fechaGuardado = LocalDateTime.now();
        this.estado = estado;
    }

    public String getId()                   { return id; }
    public LocalDateTime getFechaGuardado() { return fechaGuardado; }

    /**
     * Devuelve los identificadores de los usuarios participantes.
     */
    public List<String> getIdsUsuarios() { return new ArrayList<>(idsUsuarios); }

    /**
     * Devuelve los nombres de los usuarios participantes.
     */
    public List<String> getNombresUsuarios() { return new ArrayList<>(nombresUsuarios); }

    /**
     * Devuelve el estado almacenado de la partida.
     */
    public Burako getEstado() { return estado; }

    public boolean participaUsuario(String idUsuario) {
        return idsUsuarios.contains(idUsuario);
    }

    @Override
    public String toString() {
        return id + " | " + String.join(" vs ", nombresUsuarios) + " | " + fechaGuardado;
    }
}
