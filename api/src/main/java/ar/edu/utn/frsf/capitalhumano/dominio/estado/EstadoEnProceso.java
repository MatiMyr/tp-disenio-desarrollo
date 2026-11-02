package ar.edu.utn.frsf.capitalhumano.dominio.estado;

import java.time.LocalDateTime;
import java.util.Map;

import ar.edu.utn.frsf.capitalhumano.dominio.BloquePreguntas;
import ar.edu.utn.frsf.capitalhumano.dominio.Cuestionario;
import ar.edu.utn.frsf.capitalhumano.dominio.OpcionEvaluacion;
import ar.edu.utn.frsf.capitalhumano.dominio.PreguntaCuestionario;

/**
 * El candidato ya ingreso al menos una vez y le quedan bloques por responder.
 * Singleton: el estado no guarda datos propios.
 */
public class EstadoEnProceso implements IEstadoCuestionario {

    private static final EstadoEnProceso INSTANCIA = new EstadoEnProceso();

    private EstadoEnProceso() {
    }

    public static EstadoEnProceso getInstancia() {
        return INSTANCIA;
    }

    /** Reingreso: si ya uso todos los accesos permitidos queda Incompleto. */
    @Override
    public void ingresar(Cuestionario c, int tamBloque) {
        if (!c.verificarAccesos()) {
            c.setFechaHoraFin(LocalDateTime.now());
            c.setEstado(EstadoIncompleto.getInstancia());
            return;
        }
        c.registrarAcceso();
    }

    @Override
    public void responderBloque(Cuestionario c, int nroBloque, Map<Integer, Integer> respuestas) {
        BloquePreguntas bloque = c.buscarBloque(nroBloque);
        if (bloque == null) {
            throw new IllegalStateException("El bloque " + nroBloque + " no existe en el cuestionario");
        }
        for (PreguntaCuestionario pc : bloque.getPreguntas()) {
            Integer idOpcion = respuestas.get(pc.getIdPreguntaCuestionario());
            if (idOpcion == null) {
                continue;
            }
            OpcionEvaluacion opcion = pc.getPreguntaEvaluacion().getOpciones().stream()
                    .filter(o -> o.getIdOpcionEvaluacion() == idOpcion)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("La opcion elegida no pertenece a la pregunta"));
            pc.responder(opcion);
        }
        bloque.setRespondido(true);
        if (c.todosLosBloquesRespondidos()) {
            c.calcularPuntaje();
            c.finalizar();
        }
    }

    @Override
    public void finalizar(Cuestionario c) {
        c.setFechaHoraFin(LocalDateTime.now());
        c.setEstado(EstadoCompleto.getInstancia());
    }

    /** Vence el tiempo maximo contado desde el primer ingreso (CU026, 1.A). */
    @Override
    public void verificarVencimiento(Cuestionario c) {
        if (c.getFechaHoraComienzo() == null) {
            return;
        }
        LocalDateTime vencimiento = c.getFechaHoraComienzo().plusMinutes(c.getTiempoMaximo());
        if (LocalDateTime.now().isAfter(vencimiento)) {
            c.setFechaHoraFin(LocalDateTime.now());
            c.setEstado(EstadoIncompleto.getInstancia());
        }
    }

    @Override
    public String obtenerEstado() {
        return "EN_PROCESO";
    }
}
