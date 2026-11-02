package ar.edu.utn.frsf.capitalhumano.dominio.estado;

import java.time.LocalDateTime;
import java.util.Map;

import ar.edu.utn.frsf.capitalhumano.dominio.Cuestionario;

/**
 * El cuestionario se genero y el candidato todavia no ingreso.
 * Singleton: el estado no guarda datos propios, asi que una sola instancia
 * alcanza para todos los cuestionarios.
 */
public class EstadoActivo implements IEstadoCuestionario {

    private static final EstadoActivo INSTANCIA = new EstadoActivo();

    private EstadoActivo() {
    }

    public static EstadoActivo getInstancia() {
        return INSTANCIA;
    }

    /** Primer ingreso: arma los bloques con la copia del modelo y pasa a En proceso. */
    @Override
    public void ingresar(Cuestionario c, int tamBloque) {
        c.armarBloques(c.getEvaluacion().obtenerModelo(), tamBloque);
        c.setFechaHoraComienzo(LocalDateTime.now());
        c.registrarAcceso();
        c.setEstado(EstadoEnProceso.getInstancia());
    }

    @Override
    public void responderBloque(Cuestionario c, int nroBloque, Map<Integer, Integer> respuestas) {
        throw new IllegalStateException("El cuestionario todavia no fue comenzado");
    }

    @Override
    public void finalizar(Cuestionario c) {
        throw new IllegalStateException("El cuestionario todavia no fue comenzado");
    }

    /** Vence el plazo del estado activo: pasa a Sin contestar (CU026, 1.B). */
    @Override
    public void verificarVencimiento(Cuestionario c) {
        LocalDateTime vencimiento = c.getFechaHoraCreacion().plusDays(c.getPlazoActivo());
        if (LocalDateTime.now().isAfter(vencimiento)) {
            c.setFechaHoraFin(LocalDateTime.now());
            c.setEstado(EstadoSinContestar.getInstancia());
        }
    }

    @Override
    public String obtenerEstado() {
        return "ACTIVO";
    }
}
