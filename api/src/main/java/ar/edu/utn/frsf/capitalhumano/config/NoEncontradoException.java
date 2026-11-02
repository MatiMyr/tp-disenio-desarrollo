package ar.edu.utn.frsf.capitalhumano.config;

/** No existe lo que se pidio. Se traduce a HTTP 404. */
public class NoEncontradoException extends RuntimeException {

    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
