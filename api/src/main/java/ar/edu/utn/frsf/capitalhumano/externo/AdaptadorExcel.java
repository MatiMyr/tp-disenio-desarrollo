package ar.edu.utn.frsf.capitalhumano.externo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

/** Adaptador de Apache POI: genera un archivo .xls en la ruta indicada. */
@Component
public class AdaptadorExcel implements IExportadorExcel {

    @Override
    public void exportar(List<List<String>> datos, String ruta) {
        try (Workbook libro = inicializarLibro(); FileOutputStream salida = new FileOutputStream(ruta)) {
            Sheet hoja = libro.createSheet("Claves");
            CellStyle estiloEncabezado = libro.createCellStyle();
            Font negrita = libro.createFont();
            negrita.setBold(true);
            estiloEncabezado.setFont(negrita);

            // Se resalta la primera fila y la que arranca cada bloque, o sea la
            // que viene después de una fila en blanco
            for (int f = 0; f < datos.size(); f++) {
                Row fila = hoja.createRow(f);
                List<String> valores = datos.get(f);
                boolean esEncabezado = f == 0 || esFilaVacia(datos.get(f - 1));
                for (int c = 0; c < valores.size(); c++) {
                    Cell celda = fila.createCell(c);
                    celda.setCellValue(valores.get(c));
                    if (esEncabezado) {
                        celda.setCellStyle(estiloEncabezado);
                    }
                }
            }
            if (!datos.isEmpty()) {
                for (int c = 0; c < datos.get(0).size(); c++) {
                    hoja.autoSizeColumn(c);
                }
            }
            libro.write(salida);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el archivo Excel", e);
        }
    }

    private boolean esFilaVacia(List<String> fila) {
        return fila.isEmpty() || fila.stream().allMatch(v -> v == null || v.isBlank());
    }

    private Workbook inicializarLibro() {
        return new HSSFWorkbook();
    }
}
