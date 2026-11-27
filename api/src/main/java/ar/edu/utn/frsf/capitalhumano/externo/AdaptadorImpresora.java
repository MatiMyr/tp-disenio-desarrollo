package ar.edu.utn.frsf.capitalhumano.externo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/** Adaptador de impresión: genera el PDF que el navegador descarga o imprime. */
@Component
public class AdaptadorImpresora implements IImpresora {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void imprimir(List<List<String>> reporte, String titulo, String ruta) {
        Document documento = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        try (FileOutputStream salida = new FileOutputStream(ruta)) {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph encabezado = new Paragraph(titulo, fuenteTitulo);
            encabezado.setSpacingAfter(6);
            documento.add(encabezado);

            Font fuentePie = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9);
            Paragraph fecha = new Paragraph(
                    "Emitido el " + LocalDateTime.now().format(FORMATO_FECHA), fuentePie);
            fecha.setSpacingAfter(14);
            documento.add(fecha);

            if (!reporte.isEmpty()) {
                documento.add(armarTabla(reporte));
            }
            documento.close();
            enviarColaImpresion();
        } catch (IOException | DocumentException e) {
            throw new UncheckedIOException(new IOException("No se pudo generar el PDF", e));
        }
    }

    private PdfPTable armarTabla(List<List<String>> reporte) {
        List<String> encabezados = reporte.get(0);
        PdfPTable tabla = new PdfPTable(encabezados.size());
        tabla.setWidthPercentage(100);

        Font fuenteEncabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font fuenteCelda = FontFactory.getFont(FontFactory.HELVETICA, 10);

        for (String encabezado : encabezados) {
            PdfPCell celda = new PdfPCell(new Phrase(encabezado, fuenteEncabezado));
            celda.setHorizontalAlignment(Element.ALIGN_LEFT);
            celda.setPadding(5);
            tabla.addCell(celda);
        }
        for (int fila = 1; fila < reporte.size(); fila++) {
            for (String valor : reporte.get(fila)) {
                PdfPCell celda = new PdfPCell(new Phrase(valor, fuenteCelda));
                celda.setPadding(5);
                tabla.addCell(celda);
            }
        }
        return tabla;
    }

    /** En esta entrega el listado se descarga: no hay cola de impresión real. */
    private void enviarColaImpresion() {
        // Sin impresora conectada
    }
}
