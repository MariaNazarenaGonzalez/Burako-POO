package ar.edu.unlu.poo.burako.modelo;

import java.util.List;

/**
 * Valida y clasifica la FORMA de combinaciones de fichas (escalera, pierna).
 *
 * Renombrado de ValidadorJuego a ValidadorFormacion para dejar en claro
 * su responsabilidad única: detectar si un conjunto de fichas tiene la
 * forma estructural de una escalera o pierna, y clasificarlo.
 *
 * NO decide si un jugador PUEDE bajar el juego (eso es ReglasDeJuego).
 * NO decide si es Burako limpio o sucio (eso es ReglasDeJuego).
 * Solo responde: ¿estas fichas, en este orden, forman qué combinación?
 *
 * Package-private: solo ReglasDeJuego y Juego lo usan.
 */
final class ValidadorFormacion {

    private ValidadorFormacion() {}

    // ── Clasificación ──────────────────────────────────────────────────────────

    /**
     * Determina el TipoJuego de una lista de fichas.
     * @throws Exception si la combinación no es válida o la cantidad está fuera de rango.
     */
    static TipoJuego clasificar(List<Ficha> fichas) throws Exception {
        int cant = fichas.size();
        if (cant < 3) {
            throw new Exception("Se necesitan al menos 3 fichas para formar un juego (se enviaron " + cant + ").");
        }
        if (cant > 13) {
            throw new Exception("Un juego no puede tener más de 13 fichas (se enviaron " + cant + ").");
        }

        boolean esEscalera = esEscalera(fichas);
        boolean esPierna   = esPierna(fichas);

        if (!esEscalera && !esPierna) {
            throw new Exception("Las fichas no forman una escalera (mismo color, números consecutivos) "
                    + "ni una pierna (mismo número, distintos colores).");
        }

        if (cant < 7) {
            return esEscalera ? TipoJuego.Escalera : TipoJuego.Pierna;
        }

        // 7+ fichas → canasta; la pureza la determina ReglasDeJuego, aquí solo clasificamos
        if (esEscalera) {
            return esPuraEscalera(fichas) ? TipoJuego.CanastaPuraEscalera
                                          : TipoJuego.CanastaImpuraEscalera;
        } else {
            return esPuraPierna(fichas) ? TipoJuego.CanastaPuraPierna
                                        : TipoJuego.CanastaImpuraPierna;
        }
    }

    /**
     * Reclasifica tras agregar una ficha cuando el total ya llega a 7+.
     */
    static TipoJuego reclasificarComoCanasta(List<Ficha> fichas) {
        if (esEscalera(fichas)) {
            return esPuraEscalera(fichas) ? TipoJuego.CanastaPuraEscalera
                                          : TipoJuego.CanastaImpuraEscalera;
        } else {
            return esPuraPierna(fichas) ? TipoJuego.CanastaPuraPierna
                                        : TipoJuego.CanastaImpuraPierna;
        }
    }

    // ── Validación de adición ──────────────────────────────────────────────────

    /**
     * Verifica si {@code ficha} puede agregarse en la posición {@code pos} (0-based)
     * del juego cuyo contenido y tipo se pasan.
     */
    static boolean esAgregadoValido(List<Ficha> fichas, TipoJuego tipo, Ficha ficha, int pos) {
        boolean esEscaleraDestino =
                tipo == TipoJuego.Escalera
                || tipo == TipoJuego.CanastaImpuraEscalera
                || tipo == TipoJuego.CanastaPuraEscalera;
        boolean esPiernaDestino =
                tipo == TipoJuego.Pierna
                || tipo == TipoJuego.CanastaImpuraPierna
                || tipo == TipoJuego.CanastaPuraPierna;

        if (esEscaleraDestino) return esAgregadoValidoEscalera(fichas, ficha, pos);
        if (esPiernaDestino)   return esAgregadoValidoPierna(fichas, ficha, pos);
        return false;
    }

    // ── Escalera ───────────────────────────────────────────────────────────────

    static boolean esEscalera(List<Ficha> fichas) {
        if (fichas.isEmpty()) return false;

        Ficha primera = fichas.get(0);
        FichaColor  color;
        FichaNumero numActual;
        int inicio;

        if (primera.esComodin()) {
            if (fichas.size() < 2) return false;
            Ficha segunda = fichas.get(1);
            if (segunda.esComodin()) return false;
            color     = segunda.getColor();
            numActual = segunda.getNum();
            inicio    = 2;
        } else {
            color     = primera.getColor();
            numActual = primera.getNum();
            inicio    = 1;
        }

        for (int i = inicio; i < fichas.size(); i++) {
            Ficha f = fichas.get(i);
            FichaNumero sig = numActual.siguiente();
            if (f.esComodin()) {
                // Comodín ocupa la posición del siguiente número
                if (sig == null) return false;
                numActual = sig;
                continue;
            }
            if (sig == null)               return false;
            if (f.getColor() != color)     return false;
            if (f.getNum()   != sig)       return false;
            numActual = f.getNum();
        }
        return true;
    }

    static boolean esPierna(List<Ficha> fichas) {
        if (fichas.isEmpty()) return false;

        Ficha primera = fichas.get(0);
        FichaNumero numRef;

        if (primera.esComodin()) {
            if (fichas.size() < 2) return false;
            Ficha segunda = fichas.get(1);
            if (segunda.esComodin()) return false;
            numRef = segunda.getNum();
        } else {
            numRef = primera.getNum();
        }

        for (int i = 1; i < fichas.size(); i++) {
            Ficha f = fichas.get(i);
            if (!f.esComodin() && f.getNum() != numRef) return false;
        }
        return true;
    }

    private static boolean esPuraEscalera(List<Ficha> fichas) {
        for (int i = 0; i < fichas.size(); i++) {
            Ficha f = fichas.get(i);
            if (f.getNum() == FichaNumero.Comodin) return false;
            if (f.getNum() == FichaNumero.N2) {
                boolean siguienteEsN3 = i + 1 < fichas.size()
                        && fichas.get(i + 1).getNum() == FichaNumero.N3;
                if (!siguienteEsN3) return false;
            }
        }
        return true;
    }

    private static boolean esPuraPierna(List<Ficha> fichas) {
        long comodinesNegros = fichas.stream()
                .filter(f -> f.getNum() == FichaNumero.Comodin).count();
        if (comodinesNegros > 0) return false;
        long doses  = fichas.stream().filter(f -> f.getNum() == FichaNumero.N2).count();
        long otros  = fichas.stream().filter(f -> f.getNum() != FichaNumero.N2).count();
        return doses == 0 || otros == 0;
    }

    private static boolean esAgregadoValidoEscalera(List<Ficha> fichas, Ficha ficha, int pos) {
        if (ficha.esComodin()) return true;
        int size = fichas.size();

        if (pos == 0) {
            Ficha ref = fichas.get(0).esComodin() ? fichas.get(1) : fichas.get(0);
            return ficha.getColor() == ref.getColor()
                    && ficha.getNum().siguiente() == ref.getNum();
        } else if (pos == size) {
            Ficha ref = fichas.get(size - 1).esComodin()
                    ? fichas.get(size - 2) : fichas.get(size - 1);
            return ficha.getColor() == ref.getColor()
                    && ref.getNum().siguiente() == ficha.getNum();
        } else {
            if (!fichas.get(pos).esComodin()) return false;
            Ficha ant = fichas.get(pos - 1).esComodin()
                    ? fichas.get(pos - 2) : fichas.get(pos - 1);
            return ficha.getColor() == ant.getColor()
                    && ant.getNum().siguiente() == ficha.getNum();
        }
    }

    private static boolean esAgregadoValidoPierna(List<Ficha> fichas, Ficha ficha, int pos) {
        if (ficha.esComodin()) return true;
        Ficha ref = fichas.get(0).esComodin() ? fichas.get(1) : fichas.get(0);
        return ficha.getNum() == ref.getNum();
    }
}
