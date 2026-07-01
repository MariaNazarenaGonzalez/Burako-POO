package ar.edu.unlu.poo.burako.persistencia;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de {@link IRepositorioUsuarios} que persiste todos los
 * usuarios registrados en un único archivo serializado, conteniendo un
 * {@code HashMap<id, Usuario>}.
 *
 * Responsabilidad única: altas, búsquedas y actualizaciones de Usuario.
 * No conoce Ranking ni Partidas (eso es responsabilidad de los otros
 * repositorios, coordinados por {@link PersistenciaService}).
 *
 * Se usa un único archivo (en lugar de uno por usuario) porque la cantidad
 * de usuarios registrados es habitualmente pequeña y las consultas más
 * frecuentes (buscar por nombre, listar todos) se resuelven mejor teniendo
 * la colección completa en memoria.
 */
public class RepositorioUsuarios implements IRepositorioUsuarios {

    private final File archivo;

    public RepositorioUsuarios(File archivo) {
        this.archivo = archivo;
    }

    @Override
    public Usuario registrar(String nombre) {
        if (buscarPorNombre(nombre).isPresent()) {
            throw new PersistenciaException("Ya existe un usuario registrado con el nombre '" + nombre + "'.");
        }
        Usuario nuevo = new Usuario(nombre);
        HashMap<String, Usuario> usuarios = cargarMapa();
        usuarios.put(nuevo.getId(), nuevo);
        guardarMapa(usuarios);
        return nuevo;
    }

    @Override
    public void guardar(Usuario usuario) {
        HashMap<String, Usuario> usuarios = cargarMapa();
        usuarios.put(usuario.getId(), usuario);
        guardarMapa(usuarios);
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return Optional.ofNullable(cargarMapa().get(id));
    }

    @Override
    public Optional<Usuario> buscarPorNombre(String nombre) {
        return cargarMapa().values().stream()
                .filter(u -> u.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }

    @Override
    public List<Usuario> listarTodos() {
        return new ArrayList<>(cargarMapa().values());
    }

    private HashMap<String, Usuario> cargarMapa() {
        if (!Serializador.existe(archivo)) {
            return new HashMap<>();
        }
        return Serializador.leer(archivo);
    }

    private void guardarMapa(HashMap<String, Usuario> usuarios) {
        Serializador.guardar(archivo, usuarios);
    }
}
