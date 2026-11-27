package ar.edu.utn.frsf.capitalhumano.dto;

import java.util.List;

/**
 * Un informe listo para imprimir: encabezado, bloque de datos, secciones con
 * sus tablas, resumen y firma. La impresora no sabe de qué caso de uso viene.
 */
public record InformeDTO(String titulo, String subtitulo, String numero, String fechaEmision,
                         List<ParDTO> datos, List<SeccionInformeDTO> secciones,
                         List<ParDTO> resumen, String firmante, String pie) {
}
