package ar.edu.utn.frsf.capitalhumano.dto;

import java.util.List;

/** Alta y modificación de una pregunta, con sus opciones de respuesta. */
public record PreguntaDetalleDTO(String codigo, String nombrePregunta, String enunciado,
                                 String descripcion, String codigoCompetencia, String codigoFactor,
                                 List<PonderacionOpcionDTO> opciones) {

    public List<PonderacionOpcionDTO> opciones() {
        return opciones == null ? List.of() : opciones;
    }
}
