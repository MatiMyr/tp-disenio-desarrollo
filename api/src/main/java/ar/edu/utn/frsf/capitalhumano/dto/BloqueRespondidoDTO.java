package ar.edu.utn.frsf.capitalhumano.dto;

import java.util.List;

/** Las respuestas de un bloque que manda el candidato. */
public record BloqueRespondidoDTO(int idCuestionario, int nroBloque,
                                  List<RespuestaSeleccionadaDTO> respuestas) {

    public List<RespuestaSeleccionadaDTO> respuestas() {
        return respuestas == null ? List.of() : respuestas;
    }
}
