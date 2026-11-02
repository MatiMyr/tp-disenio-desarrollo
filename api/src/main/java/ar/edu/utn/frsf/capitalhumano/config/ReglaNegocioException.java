package ar.edu.utn.frsf.capitalhumano.config;

import java.util.List;

/**
 * Una regla de negocio impide la operacion, aunque los datos sean validos (por
 * ejemplo, el candidato ya tiene un cuestionario activo). Se traduce a HTTP 409.
 */
public class ReglaNegocioException extends RuntimeException {

    private final List<String> errores;

    public ReglaNegocioException(List<String> errores) {
        super(String.join(" | ", errores));
        this.errores = errores;
    }

    public ReglaNegocioException(String error) {
        this(List.of(error));
    }

    public List<String> getErrores() {
        return errores;
    }
}
