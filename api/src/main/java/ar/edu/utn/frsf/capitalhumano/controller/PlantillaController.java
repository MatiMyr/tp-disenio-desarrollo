package ar.edu.utn.frsf.capitalhumano.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frsf.capitalhumano.dto.PlantillaRespuestaDetalleDTO;
import ar.edu.utn.frsf.capitalhumano.dto.PlantillaRespuestaDTO;
import ar.edu.utn.frsf.capitalhumano.gestor.GestorPlantillasRespuesta;

/** Consulta de plantillas de respuesta; CU021 las usa para precargar la grilla. */
@RestController
@RequestMapping("/plantillas")
public class PlantillaController {

    private final GestorPlantillasRespuesta gestorPlantillasRespuesta;

    public PlantillaController(GestorPlantillasRespuesta gestorPlantillasRespuesta) {
        this.gestorPlantillasRespuesta = gestorPlantillasRespuesta;
    }

    @GetMapping
    public List<PlantillaRespuestaDTO> buscarPlantillas(
            @RequestParam(required = false) String nombre) {
        return gestorPlantillasRespuesta.buscarPlantillas(nombre);
    }

    @GetMapping("/{id}")
    public PlantillaRespuestaDetalleDTO obtenerPlantilla(@PathVariable int id) {
        return gestorPlantillasRespuesta.obtenerPlantilla(id);
    }
}
