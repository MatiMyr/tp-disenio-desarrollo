package ar.edu.utn.frsf.capitalhumano.dto;

import java.time.LocalDateTime;

/**
 * Una fila del orden de mérito. Los cuestionarios que no están completos no
 * tienen puntaje (CU028, obs. 1).
 */
public record OrdenMeritoDTO(String tipoDocumento, String nroDocumento, String apellido,
                             String nombre, Float puntajeObtenido, String estadoCuestionario,
                             LocalDateTime fechaCreacion, LocalDateTime fechaInicio,
                             LocalDateTime fechaFin, LocalDateTime ultimoIngreso,
                             int cantidadAccesos, boolean apto) {
}
