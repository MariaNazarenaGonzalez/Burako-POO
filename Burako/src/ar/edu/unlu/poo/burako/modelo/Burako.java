package ar.edu.unlu.poo.burako.modelo;


import java.util.ArrayList;

public class Burako implements Observado{
    private Pozo pozo;
    private Mazo mazo;
    private ArrayList<Jugador> jugadores;
    private ArrayList<Muerto> muertos;
    private ArrayList<Observador> observadores = new ArrayList<>();
    private int turnoActual;
    private EstadoTurno estadoTurno;


    public Burako(){
        this.mazo= new Mazo();
        this.pozo= new Pozo();
        this.jugadores = new ArrayList<>();
        this.muertos = new ArrayList<>();
        this.muertos.add(new Muerto(mazo.sacar(11)));
        this.muertos.add(new Muerto(mazo.sacar(11)));
        this.jugadores.add(new Jugador( mazo.sacar(12),this));
        this.jugadores.add(new Jugador( mazo.sacar(12),this));
        this.turnoActual = 0;
        this.estadoTurno = EstadoTurno.TOMAR;
    }




    public void agregarPozo(int ficha,int turno) {
        try {
            validarTurno(turno);
            validarEstado(EstadoTurno.JUGAR, "Debes tomar una ficha antes de terminar el turno");
            Jugador jugador = getJugador(turno);
            Ficha fichaElegida = jugador.verAtril(ficha);
            jugador.sacarAtril(fichaElegida);
            this.pozo.agregar(fichaElegida);
            
            // Check for Indirect Muerto or Cut
            if (jugador.getAtril().isEmpty()) {
                if (!jugador.yaTomoMuerto() && !muertos.isEmpty()) {
                    // Indirect taking of Muerto
                    ArrayList<Ficha> fichasMuerto = muertos.remove(0).tomar();
                    jugador.agregarAtril(fichasMuerto);
                    jugador.setYaTomoMuerto(true);
                    notificarObservador(Eventos.tomarMuerto_exitoso);
                    avanzarTurno();
                } else if (jugador.yaTomoMuerto() && jugador.tieneCanasta()) {
                    // Cutting
                    this.estadoTurno = EstadoTurno.PARTIDA_TERMINADA;
                    notificarObservador(Eventos.cortar_exitoso);
                    notificarObservador(Eventos.partida_terminada);
                } else {
                    // If they can't cut but are empty, they must be forced to take a ficha or something? 
                    // The rule says: "tirar una ficha y quedarse con otra en su atril" if they don't have a canasta.
                    // This means they shouldn't have been able to discard their LAST ficha.
                    // I will throw an exception to prevent discarding the last ficha without a canasta.
                    throw new Exception("No puedes quedarte sin fichas (cortar) sin tener al menos una canasta.");
                }
            } else {
                avanzarTurno();
            }
            
            notificarObservador(Eventos.agregarPozo_exitoso);
        } catch (Exception e) {
            notificarObservador(Eventos.agregarPozo_NO_exitoso,e);
        }
    }

    public String getPuntajesFinales() {
        StringBuilder sb = new StringBuilder("Partida Terminada!\nResultados:\n");
        int ganador = -1;
        // If the game ended by cutting, the player who JUST played (previous turn) is the winner.
        if (estadoTurno == EstadoTurno.PARTIDA_TERMINADA) {
             ganador = (turnoActual); // Since we didn't call avanzarTurno() when cutting
        }
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            int puntos = j.calcularPuntaje(i == ganador);
            sb.append(j.getNombre()).append(": ").append(puntos).append(" puntos\n");
        }
        return sb.toString();
    }

    public Jugador getJugador(int numero) {
        return jugadores.get(numero);
    }

    @Override
    public void agregarObservador(Observador obsevador) {
        this.observadores.add(obsevador);
    }

    @Override
    public void notificarObservador(Eventos eventos) {
        for(Observador ob: this.observadores){
            ob.notificar(eventos);
        }
    }

    public void setNombres(String nombreJugador1, String nombreJugador2) {
        this.jugadores.get(0).setNombre(nombreJugador1);
        this.jugadores.get(1).setNombre(nombreJugador2);
    }

    public ArrayList<FichaMostrable> getPozo() {
        return this.pozo.get();
    }

    public boolean agarrarPozo(int turno) {
        try {
            validarTurno(turno);
            validarEstado(EstadoTurno.TOMAR, "No puedes tomar el pozo en este momento");
            ArrayList<Ficha> fichas=sacarPozo();
            if(fichas.isEmpty()){
                throw new Exception("El pozo esta vacio");
            }
            getJugador(turno).agregarAtril(fichas);
            estadoTurno = EstadoTurno.JUGAR;
            notificarObservador(Eventos.tomarPozo_exitoso);
            return true;
        } catch (Exception e) {
            notificarObservador(Eventos.tomarPozo_NO_exitoso, e);
            return false;
        }
    }

    public boolean agarrarMazo(int turno) {
        try {
            validarTurno(turno);
            validarEstado(EstadoTurno.TOMAR, "No puedes tomar una ficha en este momento");
            Ficha ficha=sacarMazo();
            if(ficha==null){
                throw new Exception("No fue posible tomar una ficha del mazo");
            }
            getJugador(turno).agregarAtril(ficha);
            estadoTurno = EstadoTurno.JUGAR;
            notificarObservador(Eventos.tomarMazo_exitoso);
            return true;
        } catch (Exception e) {
            notificarObservador(Eventos.tomarMazo_NO_exitoso, e);
            return false;
        }
    }

    public void bajarJuego(int turno, int[] listafichas) {
        try {
            validarTurno(turno);
            validarEstado(EstadoTurno.JUGAR, "Debes tomar una ficha antes de bajar un juego");
            Jugador jugador = getJugador(turno);
            jugador.bajarJuego(listafichas);
            
            // Check for Direct Muerto
            if (jugador.getAtril().isEmpty()) {
                if (!jugador.yaTomoMuerto() && !muertos.isEmpty()) {
                    // Direct taking of Muerto
                    ArrayList<Ficha> fichasMuerto = muertos.remove(0).tomar();
                    jugador.agregarAtril(fichasMuerto);
                    jugador.setYaTomoMuerto(true);
                    notificarObservador(Eventos.tomarMuerto_exitoso);
                } else if (jugador.yaTomoMuerto() && jugador.tieneCanasta()) {
                    // This is only possible if they meld everything AND discard something, 
                    // or if they meld everything and it's their last action but rules say they must discard to cut?
                    // "Al terminar debe dejar una ficha en el pozo" for direct taking.
                    // "Para poder finalizar... el jugador debe tener al menos una canasta".
                    // The direct empty hand here means they must take the Muerto.
                    // If they already have the Muerto, they can't empty hand in bajarJuego and cut, 
                    // because they always have to discard one at the end?
                    // Actually, "una vez que un jugador se queda sin fichas... ha finalizado".
                    // "Por ejemplo: un jugador solo tiene una ficha y toma una del mazo. La ficha obtenida la puede colocar en uno de sus juegos y entonces tirar la ficha que tenia y cerrar la partida"
                    // So cutting happens in agregarPozo (discarding).
                }
            }
            
            notificarObservador(Eventos.bajarJuego_exitoso);
        } catch (Exception e) {
            notificarObservador(Eventos.bajarJuego_NO_exitoso, e);
        }
    }

    public void apoyarJuego(int ficha, int pos,int turno,int juego) {
        try {
            validarTurno(turno);
            validarEstado(EstadoTurno.JUGAR, "Debes tomar una ficha antes de apoyar");
            Jugador jugador = getJugador(turno);
            jugador.apoyarJuego(ficha, pos, juego);
            
            // Check for Direct Muerto
            if (jugador.getAtril().isEmpty()) {
                if (!jugador.yaTomoMuerto() && !muertos.isEmpty()) {
                    ArrayList<Ficha> fichasMuerto = muertos.remove(0).tomar();
                    jugador.agregarAtril(fichasMuerto);
                    jugador.setYaTomoMuerto(true);
                    notificarObservador(Eventos.tomarMuerto_exitoso);
                }
            }
            
            notificarObservador(Eventos.apoyarJuego_exitoso);
        } catch (Exception e) {
            notificarObservador(Eventos.apoyarJuego_NO_exitoso, e);
        }
    }

    private Ficha sacarMazo(){
        return this.mazo.sacarFicha();
    }
    private ArrayList<Ficha> sacarPozo(){
        return this.pozo.tomar();
    }


    public void notificarObservador(Eventos eventos, Exception e) {
        for(Observador ob: this.observadores){
            ob.notificar(eventos,e);
        }
    }

    public int getTurnoActual() {
        return turnoActual;
    }

    public EstadoTurno getEstadoTurno() {
        return estadoTurno;
    }

    public boolean puedeTomar(int turno) {
        return turnoActual == turno && estadoTurno == EstadoTurno.TOMAR;
    }

    public boolean puedeJugar(int turno) {
        return turnoActual == turno && estadoTurno == EstadoTurno.JUGAR;
    }

    private void validarTurno(int turno) throws Exception {
        if (turno != turnoActual) {
            throw new Exception("No es el turno del jugador " + getJugador(turno).getNombre());
        }
    }

    private void validarEstado(EstadoTurno estadoEsperado, String mensaje) throws Exception {
        if (estadoTurno != estadoEsperado) {
            throw new Exception(mensaje);
        }
    }

    private void avanzarTurno() {
        turnoActual = (turnoActual + 1) % jugadores.size();
        estadoTurno = EstadoTurno.TOMAR;
    }

}
