package ar.edu.utn.frsf.capitalhumano.gestor;

import java.util.List;

import org.springframework.stereotype.Service;

import ar.edu.utn.frsf.capitalhumano.config.NoEncontradoException;
import ar.edu.utn.frsf.capitalhumano.dao.PlantillaRespuestaDAO;
import ar.edu.utn.frsf.capitalhumano.dominio.PlantillaRespuesta;
import ar.edu.utn.frsf.capitalhumano.dto.ItemPlantillaDTO;
import ar.edu.utn.frsf.capitalhumano.dto.PlantillaRespuestaDetalleDTO;
import ar.edu.utn.frsf.capitalhumano.dto.PlantillaRespuestaDTO;

/**
 * Plantillas de respuesta. En esta etapa solo se consultan, para precargar la
 * grilla de opciones de CU021: el ABM completo (CU024 y CU029 a CU031) es de la
 * etapa de Desarrollo.
 */
@Service
public class GestorPlantillasRespuesta {

    private final PlantillaRespuestaDAO plantillaRespuestaDAO;

    public GestorPlantillasRespuesta(PlantillaRespuestaDAO plantillaRespuestaDAO) {
        this.plantillaRespuestaDAO = plantillaRespuestaDAO;
    }

    public List<PlantillaRespuestaDTO> buscarPlantillas(String nombre) {
        return plantillaRespuestaDAO.buscarPorFiltro(nombre).stream()
                .map(p -> new PlantillaRespuestaDTO(p.getIdPlantilla(), p.getNombre(), p.getDescripcion()))
                .toList();
    }

    public PlantillaRespuestaDetalleDTO obtenerPlantilla(int idPlantilla) {
        PlantillaRespuesta plantilla = plantillaRespuestaDAO.buscarPorId(idPlantilla);
        if (plantilla == null) {
            throw new NoEncontradoException("No existe la plantilla de respuesta " + idPlantilla);
        }
        List<ItemPlantillaDTO> items = plantilla.obtenerItems().stream()
                .map(i -> new ItemPlantillaDTO(i.getOrdenVisualizacion(), i.getDescripcion()))
                .toList();
        return new PlantillaRespuestaDetalleDTO(plantilla.getIdPlantilla(), plantilla.getNombre(),
                plantilla.getDescripcion(), items);
    }
}
