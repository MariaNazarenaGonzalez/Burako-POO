package ar.edu.unlu.poo.burako.modelo;

import java.util.List;

/**
 * Capa especializada que centraliza TODAS las reglas del juego Burako.
 *
 * ÚNICA FUENTE DE VERDAD para las siguientes categorías de reglas:
 *
 *   ① Robos      — quién puede tomar del mazo o del pozo y cuándo.
 *   ② Descartes  — cuándo se puede descartar al pozo.
 *   ③ Armados    — qué combinaciones de fichas forman escaleras y piernas.
 *   ④ Burako limpio — canasta sin comodines (N2 en posición natural se permite).
 *   ⑤ Burako sucio  — canasta con al menos un comodín o N2 fuera de lugar.
 *   ⑥ Muertos    — cuándo y cómo se asigna el muerto (directa/indirecta).
 *   ⑦ Fin del juego — condición de corte y condición de quiebre.
 *   ⑧ Puntajes   — cálculo del score final de cada jugador.
 *
 * Ninguna otra clase del modelo contiene lógica de estas categorías.
 * Burako, Jugador, Juego, GestorTurnos, GestorMuertos son clases estructurales
 * que delegan aquí toda decisión de negocio.
 *
 * Todos los métodos son estáticos: ReglasDeJuego no tiene estado.
 * Recibe un ContextoJugada (snapshot inmutable) o los datos mínimos necesarios.
 */
public final class ReglasDeJuego {

    private ReglasDeJuego() { /* no instanciable */ }

    // ══════════════════════════════════════════════════════════════════════════
    // ① ROBOS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Valida que el jugador pueda tomar una ficha del mazo.
     * Condiciones:
     * - Debe ser su turno.
     * - El estado debe ser TOMAR.
     * - El mazo no debe estar vacío.
     */
    public static ResultadoValidacion validarRoboMazo(ContextoJugada ctx) {
        ResultadoValidacion turno = validarTurnoYEstado(ctx, EstadoTurno.TOMAR,
                "No es tu turno.",
                "Ya tomaste tu ficha en este turno; debes jugar o descartar.");
        if (!turno.esValido()) return turno;

        if (ctx.isMazoVacio()) {
            return ResultadoValidacion.fallo("El mazo está vacío.");
        }
        return ResultadoValidacion.ok();
    }

    /**
     * Valida que el jugador pueda tomar las fichas del pozo.
     * Condiciones:
     * - Debe ser su turno.
     * - El estado debe ser TOMAR.
     * - El pozo no debe estar vacío.
     */
    public static ResultadoValidacion validarRoboPozo(ContextoJugada ctx) {
        ResultadoValidacion turno = validarTurnoYEstado(ctx, EstadoTurno.TOMAR,
                "No es tu turno.",
                "Ya tomaste tu ficha en este turno; debes jugar o descartar.");
        if (!turno.esValido()) return turno;

        if (ctx.isPozoVacio()) {
            return ResultadoValidacion.fallo("El pozo está vacío.");
        }
        return ResultadoValidacion.ok();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ② DESCARTES
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Valida que el jugador pueda descartar una ficha al pozo.
     * Condiciones:
     * - Debe ser su turno.
     * - El estado debe ser JUGAR (ya tomó ficha).
     *
     * No valida si es la última ficha ni el corte: eso lo hace
     * validarDescarteFinal() para separar la lógica de descarte normal
     * de la lógica de corte.
     */
    public static ResultadoValidacion validarDescarte(ContextoJugada ctx) {
        return validarTurnoYEstado(ctx, EstadoTurno.JUGAR,
                "No es tu turno.",
                "Debes tomar una ficha antes de descartar.");
    }

    /**
     * Valida el descarte de la última ficha (intento de corte o toma indirecta de muerto).
     * Se llama solo cuando cantFichasAtril == 1 y el jugador está por descartar.
     *
     * Casos válidos:
     * A) El jugador no tomó muerto y hay muertos disponibles
     *    → Toma indirecta: válido, el turno avanza después.
     * B) El jugador tomó muerto y tiene al menos una canasta
     *    → Corte exitoso.
     *
     * Caso inválido:
     * - Descartó la última ficha sin tener canasta y habiendo tomado el muerto.
     * - Descartó la última ficha sin tomar muerto y sin muertos disponibles.
     */
    public static ResultadoValidacion validarDescarteFinal(ContextoJugada ctx) {
        // Caso A: toma indirecta de muerto
        if (!ctx.yaTomoMuerto() && ctx.hayMuertosDisponibles()) {
            return ResultadoValidacion.ok(); // se procesará como toma indirecta
        }
        // Caso B: corte
        if (ctx.yaTomoMuerto() && ctx.tieneCanasta()) {
            return ResultadoValidacion.ok(); // corte exitoso
        }
        // Caso inválido
        if (!ctx.tieneCanasta()) {
            return ResultadoValidacion.fallo(
                    "No puedes cortar: necesitas al menos una canasta (7 o más fichas en un juego).");
        }
        return ResultadoValidacion.fallo(
                "No puedes cortar: aún no has tomado tu muerto.");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ③ ARMADOS — validación de combinaciones de fichas
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Valida que el jugador pueda bajar un nuevo juego.
     * Condiciones de turno:
     * - Debe ser su turno y haber tomado ficha (estado JUGAR).
     * Condiciones de la combinación:
     * - Mínimo 3 fichas, máximo 13.
     * - Deben formar una escalera o una pierna válida.
     */
    public static ResultadoValidacion validarBajarJuego(ContextoJugada ctx) {
        ResultadoValidacion turno = validarTurnoYEstado(ctx, EstadoTurno.JUGAR,
                "No es tu turno.",
                "Debes tomar una ficha antes de bajar un juego.");
        if (!turno.esValido()) return turno;

        List<Ficha> fichas = ctx.getFichasSeleccionadas();

        if (fichas.size() < 3) {
            return ResultadoValidacion.fallo("Un juego requiere al menos 3 fichas.");
        }
        if (fichas.size() > 13) {
            return ResultadoValidacion.fallo("Un juego no puede tener más de 13 fichas.");
        }

        return validarCombinacion(fichas);
    }

    /**
     * Valida que una ficha pueda apoyarse sobre un juego existente.
     * Condiciones de turno:
     * - Debe ser su turno y haber tomado ficha (estado JUGAR).
     * Condición adicional:
     * - El jugador debe tener al menos un juego bajado (cantJuegos > 0).
     * - La ficha debe ser compatible con el tipo del juego destino.
     *
     * @param ficha          la ficha a apoyar
     * @param posEnJuego     posición 0-based dentro del juego destino
     * @param fichasDelJuego fichas actuales del juego destino
     */
    public static ResultadoValidacion validarApoyarJuego(
            ContextoJugada ctx,
            Ficha ficha,
            int posEnJuego,
            List<Ficha> fichasDelJuego) {

        ResultadoValidacion turno = validarTurnoYEstado(ctx, EstadoTurno.JUGAR,
                "No es tu turno.",
                "Debes tomar una ficha antes de apoyar.");
        if (!turno.esValido()) return turno;

        if (ctx.getCantJuegos() == 0) {
            return ResultadoValidacion.fallo("No tienes juegos en la mesa sobre los cuales apoyar.");
        }

        TipoJuego tipo = ctx.getTipoJuegoDestino();
        if (tipo == null) {
            return ResultadoValidacion.fallo("Juego destino no especificado.");
        }

        boolean valido = ValidadorFormacion.esAgregadoValido(fichasDelJuego, tipo, ficha, posEnJuego);
        if (!valido) {
            return ResultadoValidacion.fallo(
                    "La ficha " + ficha + " no puede colocarse en la posición " + (posEnJuego + 1)
                            + " del juego de tipo " + tipo + ".");
        }
        return ResultadoValidacion.ok();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ④ BURAKO LIMPIO
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Determina si una lista de 7+ fichas forma un Burako limpio (canasta pura).
     *
     * Reglas del Burako limpio:
     * - Ninguna ficha es comodín negro (Ficha_Num.Comodin).
     * - Si es escalera: el N2 solo aparece en su posición natural (seguido de N3).
     * - Si es pierna: todos son del mismo número, sin N2 mezclado con otros.
     */
    public static boolean esBurakoLimpio(List<Ficha> fichas) {
        if (fichas.size() < 7) return false;

        // Sin comodines negros
        boolean tieneComodinNegro = fichas.stream()
                .anyMatch(f -> f.getNum() == FichaNumero.Comodin);
        if (tieneComodinNegro) return false;

        // Determinar si forma escalera o pierna, luego aplicar regla de N2
        if (ValidadorFormacion.esEscalera(fichas)) {
            return esBurakoLimpioEscalera(fichas);
        } else if (ValidadorFormacion.esPierna(fichas)) {
            return esBurakoLimpioPierna(fichas);
        }
        return false;
    }

    private static boolean esBurakoLimpioEscalera(List<Ficha> fichas) {
        for (int i = 0; i < fichas.size(); i++) {
            Ficha f = fichas.get(i);
            if (f.getNum() == FichaNumero.N2) {
                // N2 limpio solo si el siguiente en la secuencia es N3
                boolean siguienteEsN3 = (i + 1 < fichas.size())
                        && fichas.get(i + 1).getNum() == FichaNumero.N3;
                if (!siguienteEsN3) return false; // N2 actuando como comodín
            }
        }
        return true;
    }

    private static boolean esBurakoLimpioPierna(List<Ficha> fichas) {
        // Pierna limpia: todos son del mismo número. Si hay N2, TODOS deben ser N2.
        long cantN2     = fichas.stream().filter(f -> f.getNum() == FichaNumero.N2).count();
        long cantOtros  = fichas.stream().filter(f -> f.getNum() != FichaNumero.N2).count();
        return cantN2 == 0 || cantOtros == 0; // pierna pura de N2 o pierna sin N2
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ⑤ BURAKO SUCIO
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Determina si una lista de 7+ fichas forma un Burako sucio (canasta impura).
     * Una canasta es sucia si tiene exactamente 1 comodín (negro o N2 fuera de lugar)
     * y el resto sigue la forma de escalera o pierna válida.
     */
    public static boolean esBurakoSucio(List<Ficha> fichas) {
        if (fichas.size() < 7) return false;
        // Es canasta pero no es limpia
        return esCanasta(fichas) && !esBurakoLimpio(fichas);
    }

    /**
     * Determina si la combinación constituye algún tipo de canasta (limpia o sucia).
     */
    public static boolean esCanasta(List<Ficha> fichas) {
        if (fichas.size() < 7) return false;
        return ValidadorFormacion.esEscalera(fichas) || ValidadorFormacion.esPierna(fichas);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ⑥ MUERTOS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Determina si corresponde una toma DIRECTA del muerto.
     * Ocurre cuando el jugador vacía su atril al bajar o apoyar un juego
     * (antes de descartar).
     *
     * Condiciones:
     * - El atril del jugador quedó vacío.
     * - El jugador no ha tomado su muerto todavía.
     * - Hay muertos disponibles en la mesa.
     */
    public static boolean correspondeTomaMuertaDirecta(ContextoJugada ctx) {
        return ctx.getCantFichasAtril() == 0
                && !ctx.yaTomoMuerto()
                && ctx.hayMuertosDisponibles();
    }

    /**
     * Determina si corresponde una toma INDIRECTA del muerto.
     * Ocurre cuando el jugador descarta su última ficha al pozo.
     *
     * Condiciones idénticas a la directa, excepto que el trigger es el descarte.
     * El turno SÍ avanza tras la toma indirecta.
     */
    public static boolean correspondeTomaMuertoIndirecta(ContextoJugada ctx) {
        // Misma condición estructural: atril vacío + no tomó muerto + hay muertos
        // El contexto ya refleja que la ficha fue removida del atril antes de llamar.
        return ctx.getCantFichasAtril() == 0
                && !ctx.yaTomoMuerto()
                && ctx.hayMuertosDisponibles();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ⑦ FIN DEL JUEGO
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Determina si el jugador puede cerrar la partida (corte).
     *
     * Condiciones de corte:
     * - El jugador tomó su muerto.
     * - El jugador tiene al menos una canasta.
     * - El atril quedó vacío al descartar la última ficha.
     */
    public static boolean puedeCortar(ContextoJugada ctx) {
        return ctx.getCantFichasAtril() == 0
                && ctx.yaTomoMuerto()
                && ctx.tieneCanasta();
    }

    /**
     * Determina si la partida debe terminar por quiebre (mazo vacío y nadie cortó).
     * En Burako, si el mazo se agota, la partida se cierra y se calculan puntajes.
     */
    public static boolean hayQuiebrePorMazoVacio(ContextoJugada ctx) {
        return ctx.isMazoVacio();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ⑧ PUNTAJES
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Calcula el puntaje final de un jugador al terminar la partida.
     *
     * Reglas de puntaje:
     * - Si tiene canasta: puntosJuegos − puntosAtril.
     * - Si NO tiene canasta: −(puntosJuegos + puntosAtril) (todo es negativo).
     * - Bono por haber cortado (ser quien descartó la última ficha): +100.
     * - Bono por haber tomado el muerto: +100.
     * - Penalización por NO haber tomado el muerto: −100.
     *
     * Los bonos de Burako limpio (+200) e impuro (+100) se aplican
     * directamente al calcular puntosJuegos, dentro de cada Juego.
     *
     * @param juegos       puntaje acumulado de todos los juegos bajados
     * @param atrilFichas  fichas que quedaron en el atril al finalizar
     * @param tieneCanasta si el jugador tiene al menos una canasta
     * @param corto        si este jugador fue quien cortó
     * @param tomoMuerto   si el jugador tomó su muerto durante la partida
     */
    public static int calcularPuntaje(
            List<Juego>  juegos,
            List<Ficha>  atrilFichas,
            boolean      tieneCanasta,
            boolean      corto,
            boolean      tomoMuerto) {

        int puntosJuegos = juegos.stream().mapToInt(Juego::calcularPuntaje).sum();
        int puntosAtril  = atrilFichas.stream().mapToInt(Ficha::getValor).sum();

        int total = tieneCanasta
                ? puntosJuegos - puntosAtril
                : -(puntosJuegos + puntosAtril);

        if (corto)      total += 100;
        if (tomoMuerto) total += 100;
        else            total -= 100;

        return total;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CLASIFICACIÓN DE COMBINACIONES (delega a ValidadorFormacion)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Clasifica una combinación de fichas en su TipoJuego.
     * Lanza excepción si la combinación no es válida.
     */
    public static TipoJuego clasificarCombinacion(List<Ficha> fichas) throws Exception {
        return ValidadorFormacion.clasificar(fichas);
    }

    /**
     * Valida si una combinación de fichas forma una escalera o pierna válida.
     * Retorna ResultadoValidacion en lugar de lanzar excepción.
     */
    public static ResultadoValidacion validarCombinacion(List<Ficha> fichas) {
        try {
            ValidadorFormacion.clasificar(fichas);
            return ResultadoValidacion.ok();
        } catch (Exception e) {
            return ResultadoValidacion.fallo(e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // METODOS AUXILIARES PRIVADOS
    // ══════════════════════════════════════════════════════════════════════════

    private static ResultadoValidacion validarTurnoYEstado(
            ContextoJugada ctx,
            EstadoTurno estadoEsperado,
            String mensajeTurnoIncorrecto,
            String mensajeEstadoIncorrecto) {

        if (ctx.getTurnoActual() != ctx.getIndiceJugador()) {
            return ResultadoValidacion.fallo(mensajeTurnoIncorrecto);
        }
        if (ctx.getEstadoTurno() != estadoEsperado) {
            return ResultadoValidacion.fallo(mensajeEstadoIncorrecto);
        }
        return ResultadoValidacion.ok();
    }
}
