package ar.edu.unlu.poo.burako.modelo;

/**
 * Clasificación de un juego (combinación de fichas) en Burako.
 * Renombrado de Juego_tipo a TipoJuego siguiendo convenciones Java (PascalCase).
 */
public enum TipoJuego {
    Escalera,
    Pierna,
    CanastaPuraEscalera,
    CanastaPuraPierna,
    CanastaImpuraEscalera,
    CanastaImpuraPierna;

    /**
     * Indica si este tipo representa una canasta (7 o más fichas).
     */
    public boolean esCanasta() {
        return this == CanastaPuraEscalera
                || this == CanastaPuraPierna
                || this == CanastaImpuraEscalera
                || this == CanastaImpuraPierna;
    }

    /**
     * Indica si este tipo es una canasta pura (sin comodines que no sean 2 en posición natural).
     */
    public boolean esPura() {
        return this == CanastaPuraEscalera || this == CanastaPuraPierna;
    }
}
