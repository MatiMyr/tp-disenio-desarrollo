package ar.edu.utn.frsf.capitalhumano.externo;

import java.util.List;

/**
 * Impresión de un listado. La primera fila del reporte son los encabezados.
 * En la web "imprimir" es generar el PDF que descarga el navegador, por eso
 * recibe la ruta donde dejarlo (igual que IExportadorExcel).
 */
public interface IImpresora {

    void imprimir(List<List<String>> reporte, String titulo, String ruta);
}
