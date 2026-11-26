package ar.edu.utn.frsf.capitalhumano.gestor;

import java.util.List;

import org.springframework.stereotype.Service;

import ar.edu.utn.frsf.capitalhumano.dao.PuestoDAO;
import ar.edu.utn.frsf.capitalhumano.dto.PuestoDTO;

/**
 * Puestos. En esta etapa solo se usa la búsqueda, que necesita CU025; el ABM de
 * puestos (CU008 a CU011) es de la etapa de Desarrollo.
 */
@Service
public class GestorPuestos {

    private final PuestoDAO puestoDAO;

    public GestorPuestos(PuestoDAO puestoDAO) {
        this.puestoDAO = puestoDAO;
    }

    public List<PuestoDTO> buscarPuestos(String codigo, String nombre, String empresa) {
        return puestoDAO.buscarPorFiltros(codigo, nombre, empresa).stream()
                .map(p -> new PuestoDTO(p.getCodigo(), p.getNombre(), p.getEmpresa()))
                .toList();
    }
}
