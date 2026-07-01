package ar.edu.unlu.poo.burako.persistencia;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa una persona registrada en el sistema.
 *
 * Usuario NO es lo mismo que {@code modelo.Jugador}:
 * - Jugador es un participante TEMPORAL que solo existe mientras dura una
 *   partida en memoria (vive dentro de Burako y desaparece con ella).
 * - Usuario es una entidad PERSISTENTE, identificada por un id único, que
 *   existe independientemente de que esté jugando o no, y acumula
 *   estadísticas a través de múltiples partidas.
 *
 * Mantener esta distinción evita mezclar el dominio del juego (Jugador,
 * Juego) con el dominio de identidad/persistencia (Usuario), tal como
 * exige la consigna de esta fase.
 */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private String nombre;
    private final EstadisticasUsuario estadisticas;

    /** Crea un Usuario nuevo, generando un identificador único. */
    public Usuario(String nombre) {
        this(UUID.randomUUID().toString(), nombre, new EstadisticasUsuario());
    }

    /** Reconstruye un Usuario existente (usado por RepositorioUsuarios). */
    public Usuario(String id, String nombre, EstadisticasUsuario estadisticas) {
        this.id = id;
        this.nombre = nombre;
        this.estadisticas = estadisticas;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public EstadisticasUsuario getEstadisticas() {
        return estadisticas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        return id.equals(((Usuario) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre + " [" + estadisticas + "]";
    }
}
