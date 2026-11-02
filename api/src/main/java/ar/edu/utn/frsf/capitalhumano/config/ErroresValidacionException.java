package ar.edu.utn.frsf.capitalhumano.config;

import java.util.List;

/**
 * Datos invalidos. Lleva TODOS los errores juntos, porque la consigna pide
 * mostrarlos todos a la vez. Se traduce a HTTP 400.
 */
public class ErroresValidacionException extends RuntimeException {

    private final List<String> errores;

    public ErroresValidacionException(List<String> errores) {
        super(String.join(" | ", errores));
        this.errores = errores;
    }

    public ErroresValidacionException(String error) {
        this(List.of(error));
    }

    public List<String> getErrores() {
        return errores;
    }
}
