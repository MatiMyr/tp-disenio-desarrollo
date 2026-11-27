package ar.edu.utn.frsf.capitalhumano.externo;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
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
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import ar.edu.utn.frsf.capitalhumano.dto.InformeDTO;
import ar.edu.utn.frsf.capitalhumano.dto.ParDTO;
import ar.edu.utn.frsf.capitalhumano.dto.SeccionInformeDTO;

/**
 * Adaptador de impresión: arma el PDF A4 vertical con el diseño de los informes
 * de EvalPro (encabezado, datos, secciones, resumen y firma).
 */
@Component
public class AdaptadorImpresora implements IImpresora {

    private static final Color PRIMARIO = new Color(0x2F, 0x4E, 0xAB);
    private static final Color TINTA = new Color(0x1A, 0x1A, 0x1A);
    private static final Color MEDIO = new Color(0x77, 0x77, 0x77);
    private static final Color PANEL = new Color(0xEC, 0xEA, 0xE4);
    private static final Color CLARO = new Color(0xD8, 0xD4, 0xCC);
    private static final Color CEBRA = new Color(0xFA, 0xFA, 0xF8);

    @Override
    public void imprimir(InformeDTO reporte, String ruta) {
        Document documento = new Document(PageSize.A4, 46, 46, 46, 40);
        try (FileOutputStream salida = new FileOutputStream(ruta)) {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            documento.add(encabezado(reporte));
            documento.add(titulo(reporte));
            if (!reporte.datos().isEmpty()) {
                documento.add(bloqueDatos(reporte.datos()));
            }
            for (SeccionInformeDTO seccion : reporte.secciones()) {
                documento.add(bloqueSeccion(seccion));
            }
            if (!reporte.resumen().isEmpty()) {
                documento.add(bloqueResumen(reporte.resumen()));
            }
            documento.add(firma(reporte.firmante()));
            documento.add(pie(reporte));

            documento.close();
            enviarColaImpresion();
        } catch (IOException | DocumentException e) {
            throw new UncheckedIOException(new IOException("No se pudo generar el PDF", e));
        }
    }

    // ---------- Encabezado ----------

    private PdfPTable encabezado(InformeDTO reporte) {
        PdfPTable tabla = new PdfPTable(new float[] { 10, 58, 32 });
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(10);

        PdfPCell logo = new PdfPCell(new Phrase("EP",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, Color.WHITE)));
        logo.setBackgroundColor(PRIMARIO);
        logo.setHorizontalAlignment(Element.ALIGN_CENTER);
        logo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logo.setFixedHeight(34);
        logo.setBorderColor(TINTA);
        logo.setBorderWidth(1.2f);
        tabla.addCell(logo);

        Paragraph organizacion = new Paragraph();
        organizacion.add(new Phrase("EvalPro — Sistema de Evaluación de Perfiles\n",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, TINTA)));
        organizacion.add(new Phrase("Gestión de Evaluaciones",
                FontFactory.getFont(FontFactory.HELVETICA, 8, MEDIO)));
        tabla.addCell(sinBorde(organizacion, Element.ALIGN_LEFT));

        Paragraph meta = new Paragraph();
        meta.add(new Phrase("Informe N°: " + reporte.numero() + "\n",
                FontFactory.getFont(FontFactory.HELVETICA, 8, MEDIO)));
        meta.add(new Phrase("Fecha de emisión: " + reporte.fechaEmision() + "\n",
                FontFactory.getFont(FontFactory.HELVETICA, 8, MEDIO)));
        meta.add(new Phrase("Página 1 de 1", FontFactory.getFont(FontFactory.HELVETICA, 8, MEDIO)));
        tabla.addCell(sinBorde(meta, Element.ALIGN_RIGHT));

        return tabla;
    }

    private Paragraph titulo(InformeDTO reporte) {
        Paragraph parrafo = new Paragraph(reporte.titulo().toUpperCase(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, TINTA));
        parrafo.setAlignment(Element.ALIGN_CENTER);
        parrafo.setSpacingBefore(4);
        parrafo.setSpacingAfter(reporte.subtitulo() == null ? 12 : 2);
        return parrafo;
    }

    // ---------- Bloques ----------

    private PdfPTable bloqueDatos(List<ParDTO> datos) {
        PdfPTable contenido = new PdfPTable(2);
        contenido.setWidthPercentage(100);
        for (ParDTO dato : datos) {
            Paragraph linea = new Paragraph();
            linea.add(new Phrase(dato.etiqueta() + " ",
                    FontFactory.getFont(FontFactory.HELVETICA, 9, MEDIO)));
            linea.add(new Phrase(dato.valor(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TINTA)));
            PdfPCell celda = new PdfPCell();
            celda.addElement(linea);
            celda.setBorder(Rectangle.NO_BORDER);
            celda.setPadding(3);
            contenido.addCell(celda);
        }
        if (datos.size() % 2 == 1) {
            PdfPCell vacia = new PdfPCell(new Phrase(" "));
            vacia.setBorder(Rectangle.NO_BORDER);
            contenido.addCell(vacia);
        }
        return enmarcar("Datos del informe", null, contenido);
    }

    private PdfPTable bloqueSeccion(SeccionInformeDTO seccion) {
        PdfPTable tabla = new PdfPTable(seccion.encabezados().size());
        tabla.setWidthPercentage(100);

        Font fuenteEncabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, TINTA);
        for (String encabezado : seccion.encabezados()) {
            PdfPCell celda = new PdfPCell(new Phrase(encabezado.toUpperCase(), fuenteEncabezado));
            celda.setBackgroundColor(PANEL);
            celda.setBorderColor(MEDIO);
            celda.setBorderWidth(0.8f);
            celda.setPadding(4);
            tabla.addCell(celda);
        }

        Font fuenteCelda = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, TINTA);
        int fila = 0;
        for (List<String> valores : seccion.filas()) {
            for (String valor : valores) {
                PdfPCell celda = new PdfPCell(new Phrase(valor, fuenteCelda));
                celda.setBorderColor(CLARO);
                celda.setBorderWidth(0.5f);
                celda.setPadding(4);
                if (fila % 2 == 1) {
                    celda.setBackgroundColor(CEBRA);
                }
                tabla.addCell(celda);
            }
            fila++;
        }
        if (seccion.filas().isEmpty()) {
            PdfPCell vacia = new PdfPCell(new Phrase("Sin registros",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8.5f, MEDIO)));
            vacia.setColspan(seccion.encabezados().size());
            vacia.setHorizontalAlignment(Element.ALIGN_CENTER);
            vacia.setBorderColor(CLARO);
            vacia.setPadding(10);
            tabla.addCell(vacia);
        }
        return enmarcar(seccion.titulo(), seccion.aclaracion(), tabla);
    }

    private PdfPTable bloqueResumen(List<ParDTO> resumen) {
        PdfPTable contenido = new PdfPTable(resumen.size());
        contenido.setWidthPercentage(100);
        for (ParDTO dato : resumen) {
            Paragraph linea = new Paragraph();
            linea.add(new Phrase(dato.etiqueta().toUpperCase() + "\n",
                    FontFactory.getFont(FontFactory.HELVETICA, 7, MEDIO)));
            linea.add(new Phrase(dato.valor(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, TINTA)));
            PdfPCell celda = new PdfPCell();
            celda.addElement(linea);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            celda.setBackgroundColor(CEBRA);
            celda.setBorderColor(CLARO);
            celda.setBorderWidth(0.8f);
            celda.setPadding(6);
            contenido.addCell(celda);
        }
        return enmarcar("Resumen del proceso", null, contenido);
    }

    /** Pone un título de barra gris arriba y un marco alrededor del contenido. */
    private PdfPTable enmarcar(String titulo, String aclaracion, PdfPTable contenido) {
        PdfPTable marco = new PdfPTable(1);
        marco.setWidthPercentage(100);
        marco.setSpacingAfter(10);

        PdfPCell barra = new PdfPCell(new Phrase(titulo.toUpperCase(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, MEDIO)));
        barra.setBackgroundColor(PANEL);
        barra.setBorderColor(TINTA);
        barra.setBorderWidth(1f);
        barra.setPadding(5);
        marco.addCell(barra);

        if (aclaracion != null) {
            PdfPCell nota = new PdfPCell(new Phrase(aclaracion,
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, MEDIO)));
            nota.setBorderColor(TINTA);
            nota.setBorderWidth(1f);
            nota.setPaddingLeft(6);
            nota.setPaddingRight(6);
            nota.setPaddingBottom(4);
            marco.addCell(nota);
        }

        PdfPCell cuerpo = new PdfPCell(contenido);
        cuerpo.setBorderColor(TINTA);
        cuerpo.setBorderWidth(1f);
        cuerpo.setPadding(6);
        marco.addCell(cuerpo);
        return marco;
    }

    // ---------- Cierre ----------

    private PdfPTable firma(String firmante) {
        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(18);
        tabla.setSpacingAfter(10);
        tabla.addCell(sinBorde(new Paragraph(" "), Element.ALIGN_LEFT));

        Paragraph bloque = new Paragraph();
        bloque.add(new Phrase("CONSULTOR RESPONSABLE\n",
                FontFactory.getFont(FontFactory.HELVETICA, 7, MEDIO)));
        bloque.add(new Phrase(firmante, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TINTA)));
        bloque.setAlignment(Element.ALIGN_CENTER);

        PdfPCell celda = new PdfPCell();
        celda.addElement(bloque);
        celda.setBorder(Rectangle.TOP);
        celda.setBorderColorTop(TINTA);
        celda.setBorderWidthTop(1f);
        celda.setPaddingTop(4);
        tabla.addCell(celda);

        tabla.addCell(sinBorde(new Paragraph(" "), Element.ALIGN_LEFT));
        return tabla;
    }

    private PdfPTable pie(InformeDTO reporte) {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        Font fuente = FontFactory.getFont(FontFactory.HELVETICA, 7, MEDIO);

        PdfPCell izquierda = new PdfPCell(new Phrase(reporte.pie(), fuente));
        izquierda.setBorder(Rectangle.TOP);
        izquierda.setBorderColorTop(CLARO);
        izquierda.setPaddingTop(4);
        tabla.addCell(izquierda);

        PdfPCell derecha = new PdfPCell(
                new Phrase("Informe " + reporte.numero() + " · " + reporte.fechaEmision(), fuente));
        derecha.setBorder(Rectangle.TOP);
        derecha.setBorderColorTop(CLARO);
        derecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
        derecha.setPaddingTop(4);
        tabla.addCell(derecha);
        return tabla;
    }

    private PdfPCell sinBorde(Paragraph contenido, int alineacion) {
        contenido.setAlignment(alineacion);
        PdfPCell celda = new PdfPCell();
        celda.addElement(contenido);
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return celda;
    }

    /** En esta entrega el listado se descarga: no hay cola de impresión real. */
    private void enviarColaImpresion() {
        // Sin impresora conectada
    }
}
