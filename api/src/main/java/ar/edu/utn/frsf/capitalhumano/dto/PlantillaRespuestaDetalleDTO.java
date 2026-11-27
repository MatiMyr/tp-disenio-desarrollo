package ar.edu.utn.frsf.capitalhumano.dto;

import java.util.List;

/** Plantilla con sus ítems: precarga la grilla de opciones de CU021. */
public record PlantillaRespuestaDetalleDTO(int idPlantilla, String nombre, String descripcion,
                                           List<ItemPlantillaDTO> items) {
}
