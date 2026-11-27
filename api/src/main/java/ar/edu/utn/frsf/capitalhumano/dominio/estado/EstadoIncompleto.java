package ar.edu.utn.frsf.capitalhumano.dominio.estado;

import java.util.Map;

import ar.edu.utn.frsf.capitalhumano.dominio.Cuestionario;

/**
 * El candidato comenzo el cuestionario pero supero el tiempo maximo o la
 * cantidad maxima de accesos. Estado final.
 * Singleton: el estado no guarda datos propios.
 */
public class EstadoIncompleto implements IEstadoCuestionario {

    private static final EstadoIncompleto INSTANCIA = new EstadoIncompleto();

    private EstadoIncompleto() {
    }

    public static EstadoIncompleto getInstancia() {
        return INSTANCIA;
    }

    @Override
    public void ingresar(Cuestionario c, int tamBloque) {
        throw new IllegalStateException("El cuestionario quedo incompleto");
    }

    @Override
    public void responderBloque(Cuestionario c, int nroBloque, Map<Integer, Integer> respuestas) {
        throw new IllegalStateException("El cuestionario quedo incompleto");
    }

    @Override
    public void finalizar(Cuestionario c) {
        throw new IllegalStateException("El cuestionario quedo incompleto");
    }

    @Override
    public void verificarVencimiento(Cuestionario c) {
        // Estado final: ya vencio
    }

    @Override
    public String obtenerEstado() {
        return "INCOMPLETO";
    }
}
