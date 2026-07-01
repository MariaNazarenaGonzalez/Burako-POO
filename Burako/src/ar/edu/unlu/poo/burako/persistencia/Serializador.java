package ar.edu.unlu.poo.burako.persistencia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Utilidad de bajo nivel para leer y escribir objetos en disco usando
 * Serialización Java (ObjectOutputStream / ObjectInputStream).
 *
 * Responsabilidad única: saber CÓMO persistir bytes en un archivo dado.
 * No sabe QUÉ se persiste (Usuario, PartidaGuardada, Ranking, etc.) ni
 * DÓNDE conceptualmente debe guardarse cada cosa: esas decisiones son
 * responsabilidad de los repositorios (RepositorioUsuarios,
 * RepositorioRanking, RepositorioPartidas).
 *
 * Es la única clase del proyecto que invoca directamente
 * ObjectOutputStream/ObjectInputStream, evitando duplicar el manejo de
 * recursos y de excepciones de E/S en cada repositorio.
 */
public final class Serializador {

    private Serializador() {
        // Clase utilitaria, no instanciable.
    }

    /**
     * Serializa {@code objeto} en {@code archivo}, creando las carpetas
     * intermedias si no existen. Sobrescribe el archivo si ya existía.
     */
    public static <T extends Serializable> void guardar(File archivo, T objeto) {
        File carpeta = archivo.getAbsoluteFile().getParentFile();
        if (carpeta != null && !carpeta.exists() && !carpeta.mkdirs()) {
            throw new PersistenciaException("No se pudo crear el directorio: " + carpeta.getPath());
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo))) {
            oos.writeObject(objeto);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo guardar el archivo: " + archivo.getPath(), e);
        }
    }

    /**
     * Deserializa y retorna el objeto almacenado en {@code archivo}.
     * @throws PersistenciaException si el archivo no existe, está corrupto
     *         o la clase serializada ya no es compatible.
     */
    public static <T extends Serializable> T leer(File archivo) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            @SuppressWarnings("unchecked")
            T leido = (T) ois.readObject();
            return leido;
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenciaException("No se pudo leer el archivo: " + archivo.getPath(), e);
        }
    }

    /** Retorna true si {@code archivo} existe y es un archivo regular. */
    public static boolean existe(File archivo) {
        return archivo.isFile();
    }

    /** Elimina {@code archivo} si existe. Retorna true si lo eliminó. */
    public static boolean eliminar(File archivo) {
        return archivo.exists() && archivo.delete();
    }
}
