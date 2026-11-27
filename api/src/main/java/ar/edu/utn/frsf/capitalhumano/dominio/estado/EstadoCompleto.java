package ar.edu.utn.frsf.capitalhumano.dominio.estado;

import java.util.Map;

import ar.edu.utn.frsf.capitalhumano.dominio.Cuestionario;

/**
 * El candidato respondio todos los bloques. Estado final.
 * Singleton: el estado no guarda datos propios.
 */
public class EstadoCompleto implements IEstadoCuestionario {

    private static final EstadoCompleto INSTANCIA = new EstadoCompleto();

    private EstadoCompleto() {
    }

    public static EstadoCompleto getInstancia() {
        return INSTANCIA;
    }

    @Override
    public void ingresar(Cuestionario c, int tamBloque) {
        throw new IllegalStateException("El cuestionario ya fue completado");
    }

    @Override
    public void responderBloque(Cuestionario c, int nroBloque, Map<Integer, Integer> respuestas) {
        throw new IllegalStateException("El cuestionario ya fue completado");
    }

    @Override
    public void finalizar(Cuestionario c) {
        throw new IllegalStateException("El cuestionario ya fue completado");
    }

    @Override
    public void verificarVencimiento(Cuestionario c) {
        // Estado final: ya no vence
    }

    @Override
    public String obtenerEstado() {
        return "COMPLETO";
    }
}
