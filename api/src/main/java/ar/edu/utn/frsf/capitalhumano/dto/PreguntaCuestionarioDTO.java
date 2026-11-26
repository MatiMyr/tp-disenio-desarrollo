package ar.edu.utn.frsf.capitalhumano.dto;

import java.util.List;

/** Pregunta que se le muestra al candidato, con sus opciones. */
public record PreguntaCuestionarioDTO(int idPreguntaCuestionario, String enunciado,
                                      List<ItemOpcionRespuestaDTO> opciones) {
}
