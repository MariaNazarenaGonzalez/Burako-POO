package ar.edu.unlu.poo.burako.modelo;

import java.util.List;

/**
 * Responsable de validar y clasificar combinaciones de fichas según las reglas de Burako.
 *
 * NUEVA - Extraída de Juego para separar responsabilidades:
 * - Juego: contenedor de fichas + acceso a datos.
 * - ValidadorJuego: toda la lógica de reglas (escalera, pierna, pureza, canasta).
 *
 * Al ser una clase sin estado, todos sus métodos son estáticos.
 * Esto facilita el testing unitario y la reutilización.
 */
final class ValidadorJuego {

    private ValidadorJuego() {
        // Clase utilitaria, no instanciable
    }

    // ── Clasificación ──────────────────────────────────────────────────────────

    /**
     * Determina el TipoJuego de una lista de fichas.
     * @throws Exception si la combinación no es válida o tiene cantidad incorrecta.
     */
    static TipoJuego clasificar(List<Ficha> fichas) throws Exception {
        int cant = fichas.size();
        if (cant < 3 || cant > 13) {
            throw new Exception("Cantidad de fichas no válida: debe ser entre 3 y 13.");
        }

        boolean esEscalera = esEscalera(fichas);
        boolean esPierna   = esPierna(fichas);

        if (!esEscalera && !esPierna) {
            throw new Exception("Las fichas seleccionadas no forman una escalera ni una pierna.");
        }

        if (cant < 7) {
            return esEscalera ? TipoJuego.Escalera : TipoJuego.Pierna;
        }

        // 7 o más fichas → canasta
        if (esEscalera) {
            return esEscaleraPura(fichas) ? TipoJuego.CanastaPuraEscalera
                    : TipoJuego.CanastaImpuraEscalera;
        } else {
            return esPiernaPura(fichas) ? TipoJuego.CanastaPuraPierna
                    : TipoJuego.CanastaImpuraPierna;
        }
    }

    /**
     * Reclasifica el juego tras agregar una ficha (solo se llama cuando size() >= 7).
     */
    static TipoJuego reclasificarComoCanasta(List<Ficha> fichas) {
        if (esEscalera(fichas)) {
            return esEscaleraPura(fichas) ? TipoJuego.CanastaPuraEscalera
                    : TipoJuego.CanastaImpuraEscalera;
        } else {
            return esPiernaPura(fichas) ? TipoJuego.CanastaPuraPierna
                    : TipoJuego.CanastaImpuraPierna;
        }
    }

    // ── Validación de adición ──────────────────────────────────────────────────

    /**
     * Verifica si {@code ficha} puede agregarse en la posición {@code pos} (0-based)
     * de un juego del tipo dado.
     * Solo aplica a juegos no-canasta (Escalera o Pierna); las canastas aceptan
     * fichas por los extremos o reemplazan comodines.
     */
    static boolean esAgregadoValido(List<Ficha> fichas, TipoJuego tipo, Ficha ficha, int pos) {
        if (tipo == TipoJuego.Escalera || tipo == TipoJuego.CanastaImpuraEscalera) {
            return esAgregadoValidoEscalera(fichas, ficha, pos);
        } else if (tipo == TipoJuego.Pierna || tipo == TipoJuego.CanastaImpuraPierna) {
            return esAgregadoValidoPierna(fichas, ficha, pos);
        }
        // Canastas puras no aceptan más fichas (ya están completas en pureza)
        // pero en Burako sí se puede seguir apoyando; las puras siguen el mismo
        // mecanismo que las impuras.
        if (tipo == TipoJuego.CanastaPuraEscalera) {
            return esAgregadoValidoEscalera(fichas, ficha, pos);
        }
        if (tipo == TipoJuego.CanastaPuraPierna) {
            return esAgregadoValidoPierna(fichas, ficha, pos);
        }
        return false;
    }

    // ── Lógica de escalera ─────────────────────────────────────────────────────

    static boolean esEscalera(List<Ficha> fichas) {
        Ficha primera = fichas.get(0);
        FichaColor color;
        FichaNumero numBase;
        int idx = 1;

        if (primera.esComodin()) {
            // El primer elemento es comodín: el segundo define color y número base
            if (fichas.size() < 2) return false;
            Ficha segunda = fichas.get(1);
            if (segunda.esComodin()) return false; // Dos comodines al inicio no es escalera
            color   = segunda.getColor();
            numBase = segunda.getNum();
            if (numBase == FichaNumero.N2) return false; // N2 como comodín en pos 0+1 = ambiguo
            idx = 2;
        } else {
            color   = primera.getColor();
            numBase = primera.getNum();
        }

        FichaNumero numEsperado = numBase;

        for (int i = idx; i < fichas.size(); i++) {
            Ficha f = fichas.get(i);
            if (f.esComodin()) {
                // El comodín ocupa la posición del siguiente número
                numEsperado = numEsperado.siguiente();
                if (numEsperado == null) return false;
                continue;
            }
            FichaNumero sigEsperado = numEsperado.siguiente();
            if (sigEsperado == null) return false;
            if (f.getColor() != color || f.getNum() != sigEsperado) {
                return false;
            }
            numEsperado = f.getNum();
        }
        return true;
    }

    private static boolean esEscaleraPura(List<Ficha> fichas) {
        if (!esEscalera(fichas)) return false;
        for (int i = 0; i < fichas.size(); i++) {
            Ficha f = fichas.get(i);
            if (f.getNum() == FichaNumero.Comodin) return false;
            if (f.getNum() == FichaNumero.N2) {
                // El N2 es comodín a menos que el siguiente sea N3 (uso natural)
                if (i + 1 < fichas.size() && fichas.get(i + 1).getNum() == FichaNumero.N3) {
                    continue; // N2 natural en la escalera
                }
                return false; // N2 actuando como comodín
            }
        }
        return true;
    }

    private static boolean esAgregadoValidoEscalera(List<Ficha> fichas, Ficha ficha, int pos) {
        // pos es 0-based, representa la posición ANTES de la cual se inserta
        if (ficha.esComodin()) return true; // Comodín siempre puede agregarse en extremos

        int size = fichas.size();

        if (pos == 0) {
            // Agregar al inicio: la ficha debe ser la predecesora del primer elemento real
            Ficha referencia = fichas.get(0).esComodin() ? fichas.get(1) : fichas.get(0);
            FichaNumero numRef = referencia.getNum();
            // La nueva ficha debe tener siguiente == numRef
            return ficha.getColor() == referencia.getColor()
                    && ficha.getNum().siguiente() == numRef;
        } else if (pos == size) {
            // Agregar al final
            Ficha referencia = fichas.get(size - 1).esComodin()
                    ? fichas.get(size - 2) : fichas.get(size - 1);
            return ficha.getColor() == referencia.getColor()
                    && referencia.getNum().siguiente() == ficha.getNum();
        } else {
            // Agregar en medio (reemplazando comodín): solo si la posición tiene un comodín
            if (!fichas.get(pos).esComodin()) return false;
            Ficha ant = fichas.get(pos - 1).esComodin() ? fichas.get(pos - 2) : fichas.get(pos - 1);
            return ficha.getColor() == ant.getColor()
                    && ant.getNum().siguiente() == ficha.getNum();
        }
    }

    // ── Lógica de pierna ───────────────────────────────────────────────────────

    static boolean esPierna(List<Ficha> fichas) {
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
            if (!f.esComodin() && f.getNum() != numRef) {
                return false;
            }
        }
        return true;
    }

    private static boolean esPiernaPura(List<Ficha> fichas) {
        if (!esPierna(fichas)) return false;
        // Pura: sin comodín negro y el N2, si aparece, debe aparecer doble (natural)
        long comodinesNegros = fichas.stream()
                .filter(f -> f.getNum() == FichaNumero.Comodin).count();
        if (comodinesNegros > 0) return false;

        // Si hay N2, todos deben ser N2 (pierna de doses)
        long doses = fichas.stream().filter(f -> f.getNum() == FichaNumero.N2).count();
        if (doses > 0 && doses != fichas.size()) return false; // hay mezcla

        return true;
    }

    private static boolean esAgregadoValidoPierna(List<Ficha> fichas, Ficha ficha, int pos) {
        if (ficha.esComodin()) return true;
        Ficha ref = fichas.get(0).esComodin() ? fichas.get(1) : fichas.get(0);
        return ficha.getNum() == ref.getNum();
    }
}