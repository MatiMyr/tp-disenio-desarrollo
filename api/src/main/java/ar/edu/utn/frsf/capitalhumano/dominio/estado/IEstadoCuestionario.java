package ar.edu.utn.frsf.capitalhumano.dominio.estado;

import java.util.Map;

import ar.edu.utn.frsf.capitalhumano.dominio.Cuestionario;

/**
 * Patron State: el cuestionario delega en su estado actual y cada estado
 * concreto conoce solo sus propias transiciones validas.
 */
public interface IEstadoCuestionario {

    void ingresar(Cuestionario c, int tamBloque);

    /** respuestas: id de la pregunta del cuestionario -> id de la opcion elegida. */
    void responderBloque(Cuestionario c, int nroBloque, Map<Integer, Integer> respuestas);

    void finalizar(Cuestionario c);

    void verificarVencimiento(Cuestionario c);

    String obtenerEstado();
}
