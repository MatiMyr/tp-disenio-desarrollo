package ar.edu.utn.frsf.capitalhumano.dominio.estado;

import java.util.Map;

import ar.edu.utn.frsf.capitalhumano.dominio.Cuestionario;

/**
 * Vencio el plazo del estado activo sin que el candidato ingresara nunca.
 * Estado final.
 * Singleton: el estado no guarda datos propios.
 */
public class EstadoSinContestar implements IEstadoCuestionario {

    private static final EstadoSinContestar INSTANCIA = new EstadoSinContestar();

    private EstadoSinContestar() {
    }

    public static EstadoSinContestar getInstancia() {
        return INSTANCIA;
    }

    @Override
    public void ingresar(Cuestionario c, int tamBloque) {
        throw new IllegalStateException("El cuestionario vencio sin ser contestado");
    }

    @Override
    public void responderBloque(Cuestionario c, int nroBloque, Map<Integer, Integer> respuestas) {
        throw new IllegalStateException("El cuestionario vencio sin ser contestado");
    }

    @Override
    public void finalizar(Cuestionario c) {
        throw new IllegalStateException("El cuestionario vencio sin ser contestado");
    }

    @Override
    public void verificarVencimiento(Cuestionario c) {
        // Estado final: ya vencio
    }

    @Override
    public String obtenerEstado() {
        return "SIN_CONTESTAR";
    }
}
