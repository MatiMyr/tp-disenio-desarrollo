package ar.edu.utn.frsf.capitalhumano.externo;

import ar.edu.utn.frsf.capitalhumano.dto.InformeDTO;

/**
 * Impresión de un informe. En la web "imprimir" es generar el PDF que descarga
 * el navegador, por eso recibe la ruta donde dejarlo.
 */
public interface IImpresora {

    void imprimir(InformeDTO reporte, String ruta);
}
