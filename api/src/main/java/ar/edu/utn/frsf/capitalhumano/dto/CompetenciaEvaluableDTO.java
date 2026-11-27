package ar.edu.utn.frsf.capitalhumano.dto;

/**
 * Competencia requerida por el puesto con el aviso de si se puede evaluar
 * (CU025, paso 9.A).
 */
public record CompetenciaEvaluableDTO(String nombreCompetencia, double ponderacionMinima,
                                      boolean evaluable) {
}
