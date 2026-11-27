package ar.edu.utn.frsf.capitalhumano.config;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce las excepciones de los gestores a respuestas HTTP. El cuerpo de los
 * errores siempre tiene la forma {"errores": [...]}, que es la que espera el
 * frontend para mostrarlos todos juntos.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(ErroresValidacionException.class)
    public ResponseEntity<Map<String, List<String>>> datosInvalidos(ErroresValidacionException e) {
        return ResponseEntity.badRequest().body(Map.of("errores", e.getErrores()));
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, List<String>>> reglaDeNegocio(ReglaNegocioException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("errores", e.getErrores()));
    }

    @ExceptionHandler(NoEncontradoException.class)
    public ResponseEntity<Map<String, List<String>>> noEncontrado(NoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("errores", List.of(e.getMessage())));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, List<String>>> estadoInvalido(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("errores", List.of(e.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, List<String>>> errorInesperado(Exception e) {
        LOG.error("Error inesperado al atender la peticion", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("errores", List.of("Ocurrio un error inesperado en el servidor")));
    }
}
