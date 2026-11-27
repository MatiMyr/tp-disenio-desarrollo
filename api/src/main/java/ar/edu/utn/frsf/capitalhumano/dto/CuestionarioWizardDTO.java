package ar.edu.utn.frsf.capitalhumano.dto;

import java.util.List;

/**
 * Un paso del cuestionario. A propósito NO lleva ponderaciones ni las
 * respuestas de los bloques anteriores: si viajaran al navegador del candidato
 * se podrían ver con las herramientas del browser.
 */
public record CuestionarioWizardDTO(int idCuestionario, int nroBloqueActual,
                                    int cantidadBloquesTotales,
                                    List<PreguntaCuestionarioDTO> preguntas) {

    /** No queda ningún bloque por responder: el cuestionario está completo. */
    public boolean terminado() {
        return nroBloqueActual == 0;
    }
}
