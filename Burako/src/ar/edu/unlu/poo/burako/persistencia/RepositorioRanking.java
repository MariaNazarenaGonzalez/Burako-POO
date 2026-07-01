package ar.edu.unlu.poo.burako.persistencia;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de {@link IRepositorioRanking} que persiste todas las
 * entradas de ranking en un único archivo serializado, conteniendo un
 * {@code HashMap<idUsuario, EntradaRanking>}.
 *
 * Responsabilidad única: mantener y ordenar las entradas de ranking.
 * No conoce Usuario más allá de los datos que necesita leer de él
 * (nombre y estadísticas) para mantener su propia proyección actualizada;
 * no conoce Juego ni Jugador en absoluto.
 */
public class RepositorioRanking implements IRepositorioRanking {

    private final File archivo;

    public RepositorioRanking(File archivo) {
        this.archivo = archivo;
    }

    @Override
    public void actualizar(Usuario usuario) {
        HashMap<String, EntradaRanking> ranking = cargarMapa();
        EntradaRanking entrada = ranking.computeIfAbsent(
                usuario.getId(), id -> new EntradaRanking(id, usuario.getNombre()));
        entrada.actualizar(usuario.getNombre(), usuario.getEstadisticas());
        guardarMapa(ranking);
    }

    @Override
    public List<EntradaRanking> obtenerRankingOrdenado() {
        return cargarMapa().values().stream()
                .sorted(Comparator.comparingInt(EntradaRanking::getPuntajeAcumulado).reversed()
                        .thenComparing(Comparator.comparingInt(EntradaRanking::getVictorias).reversed()))
                .collect(Collectors.toList());
    }

    private HashMap<String, EntradaRanking> cargarMapa() {
        if (!Serializador.existe(archivo)) {
            return new HashMap<>();
        }
        return Serializador.leer(archivo);
    }

    private void guardarMapa(HashMap<String, EntradaRanking> ranking) {
        Serializador.guardar(archivo, ranking);
    }
}
