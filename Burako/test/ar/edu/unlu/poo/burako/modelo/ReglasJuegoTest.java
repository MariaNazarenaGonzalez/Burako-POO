package ar.edu.unlu.poo.burako.modelo;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Tests de las reglas de FORMACIÓN de combinaciones de fichas: escaleras,
 * piernas, comodines, y clasificación limpia/sucia de canastas.
 * Cubre Juego (clasificación al construirse y al agregar fichas) y los
 * métodos estáticos de ReglasDeJuego que delegan en ValidadorFormacion.
 *
 * Reemplaza Test_Juego.java, actualizado a los nombres actuales
 * (FichaColor/FichaNumero/TipoJuego/getFichas(), en lugar de los
 * anteriores Ficha_Color/Ficha_Num/Juego_tipo/getJuego()).
 */
public class ReglasJuegoTest {

    // ── Escaleras ────────────────────────────────────────────────────────────

    @Test
    public void testEscaleraValida() throws Exception {
        Juego j = new Juego(TestDataFactory.crearEscalera(FichaColor.Amarillo, FichaNumero.N3, 3));
        Assert.assertEquals(TipoJuego.Escalera, j.getTipo());
    }

    @Test(expected = Exception.class)
    public void testErrorEscaleraColoresMezclados() throws Exception {
        List<Ficha> fichas = TestDataFactory.crearEscalera(FichaColor.Amarillo, FichaNumero.N3, 3);
        fichas.set(1, new Ficha(FichaColor.Rojo, FichaNumero.N4)); // color distinto
        new Juego(fichas);
    }

    @Test(expected = Exception.class)
    public void testErrorEscaleraNumerosNoSucesivos() throws Exception {
        List<Ficha> fichas = TestDataFactory.crearEscalera(FichaColor.Amarillo, FichaNumero.N3, 3);
        fichas.set(1, new Ficha(FichaColor.Amarillo, FichaNumero.N5)); // salto
        new Juego(fichas);
    }

    // ── Piernas ──────────────────────────────────────────────────────────────

    @Test
    public void testPiernaValida() throws Exception {
        Juego j = new Juego(TestDataFactory.crearPierna(FichaNumero.N1, 3));
        Assert.assertEquals(TipoJuego.Pierna, j.getTipo());
    }

    @Test(expected = Exception.class)
    public void testErrorPiernaNumerosMezclados() throws Exception {
        List<Ficha> fichas = TestDataFactory.crearPierna(FichaNumero.N3, 3);
        fichas.set(1, new Ficha(FichaColor.Rojo, FichaNumero.N4)); // no es comodín ni igual al resto
        new Juego(fichas);
    }

    // ── Comodines ────────────────────────────────────────────────────────────

    @Test
    public void testEscaleraConComodinInterno() throws Exception {
        List<Ficha> fichas = TestDataFactory.crearEscalera(FichaColor.Amarillo, FichaNumero.N3, 3);
        fichas.set(1, new Ficha(FichaColor.Negro, FichaNumero.Comodin)); // reemplaza al N4
        Juego j = new Juego(fichas);
        Assert.assertEquals(TipoJuego.Escalera, j.getTipo());
    }

    @Test
    public void testEscaleraConDosComoComodin() throws Exception {
        List<Ficha> fichas = List.of(
                new Ficha(FichaColor.Amarillo, FichaNumero.N8),
                new Ficha(FichaColor.Azul, FichaNumero.N2), // el 2 azul actúa como comodín del 9
                new Ficha(FichaColor.Amarillo, FichaNumero.N10)
        );
        Juego j = new Juego(fichas);
        Assert.assertEquals(TipoJuego.Escalera, j.getTipo());
    }

    // ── Canastas ─────────────────────────────────────────────────────────────

    @Test
    public void testTransicionACanastaPuraAlAgregarSeptimaFicha() throws Exception {
        Juego j = new Juego(TestDataFactory.crearEscalera(FichaColor.Rojo, FichaNumero.N1, 6));
        Assert.assertEquals(TipoJuego.Escalera, j.getTipo());

        j.agregar(new Ficha(FichaColor.Rojo, FichaNumero.N7), 7);
        Assert.assertEquals(TipoJuego.CanastaPuraEscalera, j.getTipo());
    }

    @Test
    public void testCanastaImpuraPorComodin() throws Exception {
        List<Ficha> fichas = TestDataFactory.crearEscalera(FichaColor.Rojo, FichaNumero.N1, 6);
        fichas.add(new Ficha(FichaColor.Negro, FichaNumero.Comodin));
        Juego j = new Juego(fichas);
        Assert.assertEquals(TipoJuego.CanastaImpuraEscalera, j.getTipo());
    }

    @Test
    public void testPuntajeCanastaPiernaPura() throws Exception {
        Juego j = new Juego(TestDataFactory.crearPierna(FichaNumero.N1, 7));
        Assert.assertEquals(TipoJuego.CanastaPuraPierna, j.getTipo());
        // Cada 1 vale 15 pts: 15*7 = 105. Bono canasta pura = 200. Total = 305.
        Assert.assertEquals(305, j.calcularPuntaje());
    }

    // ── Agregar / desplazamiento de comodines ──────────────────────────────

    @Test
    public void testAgregarAlInicioDeEscalera() throws Exception {
        Juego j = new Juego(TestDataFactory.crearEscalera(FichaColor.Amarillo, FichaNumero.N4, 3));
        j.agregar(new Ficha(FichaColor.Amarillo, FichaNumero.N3), 1);

        Assert.assertEquals(4, j.getFichas().size());
        Assert.assertEquals(FichaNumero.N3, j.getFichas().get(0).getNum());
    }

    @Test
    public void testAgregarComodinAlInicioDeEscalera() throws Exception {
        Juego j = new Juego(TestDataFactory.crearEscalera(FichaColor.Amarillo, FichaNumero.N4, 3));
        j.agregar(new Ficha(FichaColor.Negro, FichaNumero.Comodin), 1);

        Assert.assertEquals(4, j.getFichas().size());
        Assert.assertEquals(FichaNumero.Comodin, j.getFichas().get(0).getNum());
    }

    @Test
    public void testReemplazarComodinAlInicioLoDesplazaAlFinal() throws Exception {
        List<Ficha> fichas = List.of(
                new Ficha(FichaColor.Negro, FichaNumero.Comodin), // representa al N3
                new Ficha(FichaColor.Amarillo, FichaNumero.N4),
                new Ficha(FichaColor.Amarillo, FichaNumero.N5)
        );
        Juego j = new Juego(fichas);
        j.agregar(new Ficha(FichaColor.Amarillo, FichaNumero.N3), 1);

        Assert.assertEquals(4, j.getFichas().size());
        Assert.assertEquals(FichaNumero.N3, j.getFichas().get(0).getNum());
        Assert.assertEquals(FichaNumero.Comodin, j.getFichas().get(3).getNum()); // desplazado al final
    }

    @Test
    public void testEscaleraCircularElReyPrecedeAlAs() throws Exception {
        Juego j = new Juego(TestDataFactory.crearEscalera(FichaColor.Amarillo, FichaNumero.N1, 3)); // As,2,3
        // En Burako, el 13 (Rey) puede ir antes del As.
        j.agregar(new Ficha(FichaColor.Amarillo, FichaNumero.N13), 1);

        Assert.assertEquals(4, j.getFichas().size());
        Assert.assertEquals(FichaNumero.N13, j.getFichas().get(0).getNum());
    }

    // ── ReglasDeJuego: clasificación limpia/sucia y canasta ─────────────────

    @Test
    public void testEsBurakoLimpioParaCanastaSinComodines() throws Exception {
        List<Ficha> canasta = TestDataFactory.crearCanastaLimpia(FichaColor.Rojo, FichaNumero.N3);
        Assert.assertTrue(ReglasDeJuego.esBurakoLimpio(canasta));
        Assert.assertFalse(ReglasDeJuego.esBurakoSucio(canasta));
        Assert.assertTrue(ReglasDeJuego.esCanasta(canasta));
    }

    @Test
    public void testEsBurakoSucioParaCanastaConComodin() throws Exception {
        List<Ficha> canasta = TestDataFactory.crearCanastaSucia(FichaColor.Rojo, FichaNumero.N3);
        Assert.assertFalse(ReglasDeJuego.esBurakoLimpio(canasta));
        Assert.assertTrue(ReglasDeJuego.esBurakoSucio(canasta));
        Assert.assertTrue(ReglasDeJuego.esCanasta(canasta));
    }

    @Test
    public void testMenosDeSieteFichasNuncaEsCanasta() throws Exception {
        List<Ficha> escalera = TestDataFactory.crearEscalera(FichaColor.Rojo, FichaNumero.N3, 6);
        Assert.assertFalse(ReglasDeJuego.esCanasta(escalera));
        Assert.assertFalse(ReglasDeJuego.esBurakoLimpio(escalera));
        Assert.assertFalse(ReglasDeJuego.esBurakoSucio(escalera));
    }

    @Test
    public void testValidarApoyarJuegoAceptaFichaAlFinalDeEscalera() throws Exception {
        List<Ficha> fichasDelJuego = TestDataFactory.crearEscalera(FichaColor.Amarillo, FichaNumero.N4, 3);
        Ficha nueva = new Ficha(FichaColor.Amarillo, FichaNumero.N7);

        boolean valido = ValidadorFormacion.esAgregadoValido(
                fichasDelJuego, TipoJuego.Escalera, nueva, fichasDelJuego.size());
        Assert.assertTrue(valido);
    }

    @Test
    public void testValidarApoyarJuegoRechazaFichaIncompatible() throws Exception {
        List<Ficha> fichasDelJuego = TestDataFactory.crearEscalera(FichaColor.Amarillo, FichaNumero.N4, 3);
        Ficha incompatible = new Ficha(FichaColor.Rojo, FichaNumero.N7); // color distinto

        boolean valido = ValidadorFormacion.esAgregadoValido(
                fichasDelJuego, TipoJuego.Escalera, incompatible, fichasDelJuego.size());
        Assert.assertFalse(valido);
    }
}
