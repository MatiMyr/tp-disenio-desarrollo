package ar.edu.utn.frsf.capitalhumano.dto;

/**
 * Lo que ve el candidato al ingresar: en qué estado está su cuestionario, el
 * tiempo que le queda y las instrucciones.
 */
public record CandidatoCuestionarioDTO(String nombreCandidato, int idCuestionario, String estado,
                                       int tiempoRestante, String instrucciones) {
}
