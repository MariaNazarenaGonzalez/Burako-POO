package ar.edu.unlu.poo.burako.persistencia;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para {@link Usuario}.
 *
 * Se define como interfaz (Principio de Inversión de Dependencias) para que
 * {@link PersistenciaService} dependa de esta abstracción y no de la
 * implementación concreta basada en archivos, facilitando reemplazos
 * futuros (otra estrategia de almacenamiento) o el uso de un mock al testear.
 */
public interface IRepositorioUsuarios {

    /**
     * Registra un nuevo usuario con el nombre dado.
     * @throws PersistenciaException si ya existe un usuario con ese nombre.
     */
    Usuario registrar(String nombre);

    /** Persiste los cambios realizados sobre un usuario ya existente. */
    void guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(String id);

    Optional<Usuario> buscarPorNombre(String nombre);

    List<Usuario> listarTodos();
}
