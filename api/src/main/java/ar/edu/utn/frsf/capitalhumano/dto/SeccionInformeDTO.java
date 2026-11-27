package ar.edu.utn.frsf.capitalhumano.dto;

import java.util.List;

/** Un bloque del informe con su tabla. */
public record SeccionInformeDTO(String titulo, String aclaracion, List<String> encabezados,
                                List<List<String>> filas) {
}
