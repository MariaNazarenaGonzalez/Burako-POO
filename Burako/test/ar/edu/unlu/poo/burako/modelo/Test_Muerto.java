package ar.edu.unlu.poo.burako.modelo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;

public class Test_Muerto {
    private Burako burako;
    private Jugador j1;
    private Jugador j2;

    @Before
    public void setUp() {
        burako = new Burako();
        j1 = burako.getJugador(0);
        j2 = burako.getJugador(1);
    }

    @Test
    public void testTomaDirectaMuerto() throws Exception {
        // Vaciar atril j1 para simular toma directa
        ArrayList<FichaMostrable> atrilActual = new ArrayList<>(j1.getAtril());
        for (FichaMostrable f : atrilActual) {
            j1.sacarAtril((Ficha) f);
        }

        // Agregar fichas para un juego de 3
        j1.agregarAtril(new Ficha(Ficha_Color.Amarillo, Ficha_Num._3_));
        j1.agregarAtril(new Ficha(Ficha_Color.Amarillo, Ficha_Num._4_));
        j1.agregarAtril(new Ficha(Ficha_Color.Amarillo, Ficha_Num._5_));

        // El jugador debe haber tomado del mazo antes de jugar
        burako.agarrarMazo(0); 
        // Ahora tiene 4 fichas. Bajamos 3.
        int[] indices = {1, 2, 3};
        burako.bajarJuego(0, indices);

        // Aún le queda 1 ficha (la que tomó del mazo). Bajémosla apoyando (si fuera posible) o simplemente vaciando.
        // Para simplificar, forzamos que se quede sin fichas bajando un juego de 4.
        atrilActual = new ArrayList<>(j1.getAtril());
        for (FichaMostrable f : atrilActual) {
            j1.sacarAtril((Ficha) f);
        }
        j1.agregarAtril(new Ficha(Ficha_Color.Rojo, Ficha_Num._10_));
        j1.agregarAtril(new Ficha(Ficha_Color.Rojo, Ficha_Num._11_));
        j1.agregarAtril(new Ficha(Ficha_Color.Rojo, Ficha_Num._12_));

        indices = new int[]{1, 2, 3};
        burako.bajarJuego(0, indices);

        // Verificamos que tomó el muerto (ahora tiene 11 fichas)
        Assert.assertTrue("Debería haber tomado el muerto", j1.yaTomoMuerto());
        Assert.assertEquals("El atril debería tener 11 fichas del muerto", 11, j1.getAtril().size());
        Assert.assertEquals("El estado debería seguir siendo JUGAR para toma directa", EstadoTurno.JUGAR, burako.getEstadoTurno());
    }

    @Test
    public void testTomaIndirectaMuerto() throws Exception {
        // Vaciar atril j1
        ArrayList<FichaMostrable> atrilActual = new ArrayList<>(j1.getAtril());
        for (FichaMostrable f : atrilActual) {
            j1.sacarAtril((Ficha) f);
        }

        // Le damos una sola ficha a j1
        j1.agregarAtril(new Ficha(Ficha_Color.Amarillo, Ficha_Num._1_));

        // Turno de j1: toma del mazo. Ahora tiene 2 fichas.
        burako.agarrarMazo(0);
        
        // Quitamos una ficha manualmente para que al descartar la que queda, el atril quede vacío.
        j1.sacarAtril((Ficha) j1.getAtril().get(0));
        
        // Ahora j1 tiene exactamente 1 ficha y el estado es JUGAR.
        // Al descartar esta ficha, debería ocurrir la toma indirecta del muerto.
        burako.agregarPozo(1, 0);

        Assert.assertTrue("Debería haber tomado el muerto indirectamente", j1.yaTomoMuerto());
        Assert.assertEquals("El atril debería tener 11 fichas del muerto", 11, j1.getAtril().size());
        // En toma indirecta el turno AVANZA
        Assert.assertEquals("El turno debería haber avanzado al jugador 2 (índice 1)", 1, burako.getTurnoActual());
        Assert.assertEquals("El estado debería ser TOMAR para el siguiente jugador", EstadoTurno.TOMAR, burako.getEstadoTurno());
    }

    @Test
    public void testErrorCortarSinCanasta() throws Exception {
        // j1 ya tomó el muerto
        j1.setYaTomoMuerto(true);
        // Vaciar atril excepto 1 ficha
        ArrayList<FichaMostrable> atrilActual = new ArrayList<>(j1.getAtril());
        for (int i = 0; i < atrilActual.size() - 1; i++) {
            j1.sacarAtril((Ficha) atrilActual.get(i));
        }
        
        // Estado JUGAR (simulamos que ya tomó)
        // burako.agarrarMazo(0); // Esto le daría una ficha más
        // Intentamos descartar la última ficha sin tener canasta
        // Como agregarPozo captura excepciones y notifica al observador, 
        // pero nosotros queremos ver si falla la lógica interna.
        
        // Para este test, verificaremos que el estado NO cambie a TERMINADA
        burako.agarrarMazo(0); // Ahora tiene 2 fichas.
        burako.agregarPozo(1, 0); // Descarta 1, le queda 1.
        
        // Intentamos tirar la última. Como no tiene canasta, debería lanzar error o no terminar.
        burako.agregarPozo(1, 0); 
        
        Assert.assertNotEquals("La partida no debería haber terminado sin canasta", 
                EstadoTurno.PARTIDA_TERMINADA, burako.getEstadoTurno());
    }

    @Test
    public void testCorteExitosoConCanasta() throws Exception {
        j1.setYaTomoMuerto(true);
        j1.setNombre("Jugador 1");
        
        // Crear una canasta pura para j1
        ArrayList<Ficha> fichasCanasta = new ArrayList<>();
        for (int i = 3; i <= 9; i++) {
            fichasCanasta.add(new Ficha(Ficha_Color.Rojo, Ficha_Num.values()[i-1])); // 3 al 9
        }
        j1.agregarAtril(fichasCanasta);
        int[] indices = new int[7];
        // Las fichas agregadas están al final del atril.
        int totalFichas = j1.getAtril().size();
        for(int i=0; i<7; i++) indices[i] = totalFichas - 6 + i;
        
        burako.agarrarMazo(0);
        burako.bajarJuego(0, indices);
        
        Assert.assertTrue("Debería tener canasta", j1.tieneCanasta());
        
        // Vaciar el resto del atril para poder cortar
        ArrayList<FichaMostrable> rest = new ArrayList<>(j1.getAtril());
        for(int i=0; i<rest.size()-1; i++) j1.sacarAtril((Ficha)rest.get(i));
        
        // Ahora solo tiene 1 ficha. La tira al pozo para cortar.
        burako.agregarPozo(1, 0);
        
        Assert.assertEquals("La partida debería estar terminada", 
                EstadoTurno.PARTIDA_TERMINADA, burako.getEstadoTurno());
    }

    @Test
    public void testPuntajeSinCanastaTodoNegativo() throws Exception {
        // j1 bajó un juego pero NO tiene canasta.
        ArrayList<Ficha> fichas = new ArrayList<>();
        fichas.add(new Ficha(Ficha_Color.Azul, Ficha_Num._10_)); // 10 pts
        fichas.add(new Ficha(Ficha_Color.Azul, Ficha_Num._11_)); // 10 pts
        fichas.add(new Ficha(Ficha_Color.Azul, Ficha_Num._12_)); // 10 pts
        j1.agregarAtril(fichas);
        
        burako.agarrarMazo(0);
        int totalFichas = j1.getAtril().size();
        burako.bajarJuego(0, new int[]{totalFichas-2, totalFichas-1, totalFichas});
        
        // j1 no tomó muerto (-100)
        // j1 tiene fichas en atril (supongamos 10 fichas de 5 pts = 50 pts)
        // juego bajado = 30 pts.
        // Como no tiene canasta: -(30 + 50) - 100 = -180.
        
        int puntos = j1.calcularPuntaje(false);
        Assert.assertTrue("El puntaje debería ser negativo por no tener canasta", puntos < 0);
    }
}
