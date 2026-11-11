package ar.edu.utn.frsf.capitalhumano.gestor;

import java.util.List;

import org.springframework.stereotype.Service;

import ar.edu.utn.frsf.capitalhumano.dao.CandidatoDAO;
import ar.edu.utn.frsf.capitalhumano.dto.CandidatoGestionDTO;

/**
 * Candidatos. En esta etapa solo se usa la búsqueda, que es la que arranca
 * CU025; la importación desde CSV (CU006) es de la etapa de Desarrollo.
 */
@Service
public class GestorCandidatos {

    private final CandidatoDAO candidatoDAO;

    public GestorCandidatos(CandidatoDAO candidatoDAO) {
        this.candidatoDAO = candidatoDAO;
    }

    public List<CandidatoGestionDTO> buscarCandidatos(String apellido, String nombre,
            Integer nroCandidato) {
        return candidatoDAO.buscarPorCriterios(apellido, nombre, nroCandidato).stream()
                .map(c -> new CandidatoGestionDTO(c.getNroCandidato(), c.getApellido(), c.getNombre(),
                        c.getTipoDocumento().name(), c.getNroDocumento()))
                .toList();
    }
}
