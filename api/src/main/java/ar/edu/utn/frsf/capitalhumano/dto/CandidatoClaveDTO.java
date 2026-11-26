package ar.edu.utn.frsf.capitalhumano.dto;

/** Candidato con la clave de ingreso que se generó para su cuestionario. */
public record CandidatoClaveDTO(int nroCandidato, String apellido, String nombre,
                                String tipoDocumento, String nroDocumento, String clave) {
}
