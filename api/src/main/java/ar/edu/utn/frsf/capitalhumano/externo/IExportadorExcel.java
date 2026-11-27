package ar.edu.utn.frsf.capitalhumano.externo;

import java.util.List;

/** Exportación a Excel. La primera fila de los datos son los encabezados. */
public interface IExportadorExcel {

    void exportar(List<List<String>> datos, String ruta);
}
