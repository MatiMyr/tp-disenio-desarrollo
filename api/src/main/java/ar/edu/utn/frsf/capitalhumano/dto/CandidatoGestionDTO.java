package ar.edu.utn.frsf.capitalhumano.dto;

/** Candidato como lo ve el consultor en la búsqueda de CU025. */
public record CandidatoGestionDTO(int nroCandidato, String apellido, String nombre,
                                  String tipoDocumento, String nroDocumento) {
}
