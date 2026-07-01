package ar.edu.unlu.poo.burako.persistencia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de {@link IRepositorioPartidas} que persiste cada partida
 * guardada en un archivo independiente ({@code <id>.dat} dentro de una
 * carpeta dedicada).
 *
 * A diferencia de Usuario y Ranking (un único archivo con todas las
 * entradas), aquí se usa un archivo por partida porque:
 * - Cada partida contiene el grafo completo del modelo (mazo, atriles,
 *   juegos, etc.), por lo que agruparlas todas en un solo archivo
 *   obligaría a reescribir todo el conjunto ante cada guardado.
 * - Las partidas se eliminan individualmente al finalizar (ver
 *   {@link PersistenciaService#finalizarPartida}), lo que es directo con
 *   un archivo propio y costoso si estuvieran todas mezcladas en un mapa.
 */
public class RepositorioPartidas implements IRepositorioPartidas {

    private final File carpeta;

    public RepositorioPartidas(File carpeta) {
        this.carpeta = carpeta;
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw new PersistenciaException("No se pudo crear el directorio de partidas: " + carpeta.getPath());
        }
    }

    @Override
    public void guardar(PartidaGuardada partida) {
        Serializador.guardar(archivoDe(partida.getId()), partida);
    }

    @Override
    public Optional<PartidaGuardada> buscarPorId(String id) {
        File archivo = archivoDe(id);
        if (!Serializador.existe(archivo)) {
            return Optional.empty();
        }
        PartidaGuardada partida = Serializador.leer(archivo);
        return Optional.of(partida);
    }

    @Override
    public List<PartidaGuardada> listarPorUsuario(String idUsuario) {
        List<PartidaGuardada> resultado = new ArrayList<>();
        File[] archivos = carpeta.listFiles((dir, nombre) -> nombre.endsWith(".dat"));
        if (archivos == null) {
            return resultado;
        }
        for (File archivo : archivos) {
            PartidaGuardada partida = Serializador.leer(archivo);
            if (partida.participaUsuario(idUsuario)) {
                resultado.add(partida);
            }
        }
        return resultado;
    }

    @Override
    public void eliminar(String id) {
        Serializador.eliminar(archivoDe(id));
    }

    private File archivoDe(String id) {
        return new File(carpeta, id + ".dat");
    }
}
