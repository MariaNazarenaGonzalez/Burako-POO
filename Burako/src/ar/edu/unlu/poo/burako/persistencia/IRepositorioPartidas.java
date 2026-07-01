package ar.edu.unlu.poo.burako.persistencia;

import java.util.List;
import java.util.Optional;

/** Contrato de persistencia para {@link PartidaGuardada}. */
public interface IRepositorioPartidas {

    /** Guarda (o sobrescribe, si ya existía un id igual) una partida. */
    void guardar(PartidaGuardada partida);

    Optional<PartidaGuardada> buscarPorId(String id);

    /** Lista todas las partidas guardadas en las que participa el usuario dado. */
    List<PartidaGuardada> listarPorUsuario(String idUsuario);

    /** Elimina el guardado de una partida (p. ej., al finalizar normalmente). */
    void eliminar(String id);
}
