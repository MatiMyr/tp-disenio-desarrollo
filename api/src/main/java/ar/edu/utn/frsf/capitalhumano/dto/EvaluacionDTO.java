package ar.edu.utn.frsf.capitalhumano.dto;

import java.time.LocalDateTime;

/** Evaluación del puesto, para que el consultor elija cuáles incluir. */
public record EvaluacionDTO(int idEvaluacion, LocalDateTime fechaCreacion) {
}
