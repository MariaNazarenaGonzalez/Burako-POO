package ar.edu.unlu.poo.burako.modelo;

import java.util.List;

/**
 * Representa un "muerto" en Burako: un conjunto cerrado de 11 fichas que un
 * jugador puede tomar bajo ciertas condiciones.
 *
 * MODIFICADO respecto al original:
 * - tomar() ya no retorna null cuando está vacío; lanza IllegalStateException
 *   porque un Muerto nunca debería tomarse dos veces (el control es
 *   responsabilidad de GestorMuertos). Elimina null checks en el llamador.
 * - estaDisponible() agregado para permitir consulta sin efecto secundario.
 */
public class Muerto {

    private final List<Ficha> fichas;
    private boolean tomado = false;

    public Muerto(List<Ficha> fichas) {
        this.fichas = fichas;
    }

    /**
     * Retorna las fichas de este muerto y lo marca como tomado.
     * @throws IllegalStateException si ya fue tomado.
     */
    public List<Ficha> tomar() {
        if (tomado) {
            throw new IllegalStateException("Este muerto ya fue tomado.");
        }
        tomado = true;
        return fichas;
    }

    /** Retorna true si este muerto aún no fue tomado. */
    public boolean estaDisponible() {
        return !tomado;
    }
}
