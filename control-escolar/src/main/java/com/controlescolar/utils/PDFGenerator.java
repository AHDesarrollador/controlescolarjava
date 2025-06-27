//PDFGENERATOR.java
package com.controlescolar.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.controlescolar.models.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidad para generar documentos PDF
 * Incluye reportes de calificaciones, constancias, listas de asistencia, etc.
 */
public class PDFGenerator {

    private static final Logger LOGGER = Logger.getLogger(PDFGenerator.class.getName());

    // Configuración de fuentes
    private static final PDType1Font FONT_TITLE = PDType1Font.HELVETICA_BOLD;
    private static final PDType1Font FONT_SUBTITLE = PDType1Font.HELVETICA_BOLD;
    private static final PDType1Font FONT_NORMAL = PDType1Font.HELVETICA;
    private static final PDType1Font FONT_SMALL = PDType1Font.HELVETICA;

    // Tamaños de fuente
    private static final float FONT_SIZE_TITLE = 18f;
    private static final float FONT_SIZE_SUBTITLE = 14f;
    private static final float FONT_SIZE_NORMAL = 12f;
    private static final float FONT_SIZE_SMALL = 10f;

    // Márgenes y espaciado
    private static final float MARGIN = 50f;
    private static final float LINE_SPACING = 15f;
    private static final float PARAGRAPH_SPACING = 20f;

    /**
     * Genera reporte de calificaciones de un alumno
     */
    public static boolean generateGradeReport(Alumno alumno, List<Calificacion> calificaciones,
                                              String outputPath) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = page.getMediaBox().getHeight() - MARGIN;

                // Encabezado
                yPosition = addHeader(contentStream, page, "REPORTE DE CALIFICACIONES", yPosition);
                yPosition -= PARAGRAPH_SPACING;

                // Información del alumno
                yPosition = addStudentInfo(contentStream, alumno, yPosition);
                yPosition -= PARAGRAPH_SPACING;

                // Tabla de calificaciones
                yPosition = addGradesTable(contentStream, calificaciones, yPosition);

                // Pie de página
                addFooter(contentStream, page);
            }

            document.save(outputPath);
            LOGGER.info("Reporte de calificaciones generado: " + outputPath);
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar reporte de calificaciones", e);
            return false;
        }
    }

    /**
     * Genera constancia de estudios
     */
    public static boolean generateStudyCertificate(Alumno alumno, String outputPath) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = page.getMediaBox().getHeight() - MARGIN;

                // Encabezado institucional
                yPosition = addInstitutionalHeader(contentStream, page, yPosition);
                yPosition -= PARAGRAPH_SPACING * 2;

                // Título del documento
                contentStream.beginText();
                contentStream.setFont(FONT_TITLE, FONT_SIZE_TITLE);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("CONSTANCIA DE ESTUDIOS");
                contentStream.endText();
                yPosition -= PARAGRAPH_SPACING * 2;

                // Cuerpo de la constancia
                String constanciaText = String.format(
                        "Por medio de la presente se hace constar que %s %s, " +
                                "con número de control %s, se encuentra inscrito(a) y cursando " +
                                "sus estudios en esta institución educativa.\n\n" +
                                "La presente se expide a solicitud del interesado para los fines " +
                                "que estime convenientes.\n\n" +
                                "Lugar y fecha: %s, %s",
                        alumno.getNombre(),
                        alumno.getApellidos(),
                        alumno.getNumeroControl(),
                        "Ciudad de México", // Esto debería venir de configuración
                        LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy"))
                );

                yPosition = addWrappedText(contentStream, constanciaText, MARGIN, yPosition,
                        page.getMediaBox().getWidth() - 2 * MARGIN, FONT_NORMAL, FONT_SIZE_NORMAL);

                // Espacio para firma
                yPosition -= PARAGRAPH_SPACING * 3;
                contentStream.beginText();
                contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                contentStream.newLineAtOffset(page.getMediaBox().getWidth() - 200, yPosition);
                contentStream.showText("_________________________");
                contentStream.endText();

                yPosition -= LINE_SPACING;
                contentStream.beginText();
                contentStream.setFont(FONT_SMALL, FONT_SIZE_SMALL);
                contentStream.newLineAtOffset(page.getMediaBox().getWidth() - 200, yPosition);
                contentStream.showText("Director Académico");
                contentStream.endText();

                addFooter(contentStream, page);
            }

            document.save(outputPath);
            LOGGER.info("Constancia de estudios generada: " + outputPath);
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar constancia de estudios", e);
            return false;
        }
    }

    /**
     * Genera lista de asistencia para un grupo
     */
    public static boolean generateAttendanceList(Grupo grupo, List<Alumno> alumnos,
                                                 LocalDate fecha, String outputPath) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = page.getMediaBox().getHeight() - MARGIN;

                // Encabezado
                yPosition = addHeader(contentStream, page, "LISTA DE ASISTENCIA", yPosition);
                yPosition -= PARAGRAPH_SPACING;

                // Información del grupo
                contentStream.beginText();
                contentStream.setFont(FONT_SUBTITLE, FONT_SIZE_SUBTITLE);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Grupo: " + grupo.getNombre());
                contentStream.endText();
                yPosition -= LINE_SPACING;

                contentStream.beginText();
                contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Fecha: " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                contentStream.endText();
                yPosition -= PARAGRAPH_SPACING;

                // Tabla de asistencia
                yPosition = addAttendanceTable(contentStream, alumnos, yPosition, page);

                addFooter(contentStream, page);
            }

            document.save(outputPath);
            LOGGER.info("Lista de asistencia generada: " + outputPath);
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar lista de asistencia", e);
            return false;
        }
    }

    /**
     * Genera reporte de pagos de un alumno
     */
    public static boolean generatePaymentReport(Alumno alumno, List<Pago> pagos, String outputPath) {
        PDDocument document = null;
        try {
            document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = page.getMediaBox().getHeight() - MARGIN;

                // Encabezado
                yPosition = addHeader(contentStream, page, "ESTADO DE CUENTA", yPosition);
                yPosition -= PARAGRAPH_SPACING;

                // Información del alumno
                yPosition = addStudentInfo(contentStream, alumno, yPosition);
                yPosition -= PARAGRAPH_SPACING;

                // Tabla de pagos
                yPosition = addPaymentsTable(contentStream, pagos, yPosition);

                addFooter(contentStream, page);
            }

            // Forzar liberación de memoria antes de guardar
            System.gc();
            
            document.save(outputPath);
            LOGGER.info("Reporte de pagos generado: " + outputPath);
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar reporte de pagos", e);
            return false;
        } finally {
            // Asegurar que el documento se cierre
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error al cerrar documento PDF", e);
                }
            }
        }
    }

    /**
     * Genera reporte estadístico general
     */
    public static boolean generateStatisticsReport(Map<String, Object> estadisticas, String outputPath) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = page.getMediaBox().getHeight() - MARGIN;

                // Encabezado
                yPosition = addHeader(contentStream, page, "REPORTE ESTADÍSTICO", yPosition);
                yPosition -= PARAGRAPH_SPACING;

                // Período
                contentStream.beginText();
                contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Período: " + estadisticas.getOrDefault("periodo", "N/A"));
                contentStream.endText();
                yPosition -= PARAGRAPH_SPACING;

                // Estadísticas
                yPosition = addStatistics(contentStream, estadisticas, yPosition);

                addFooter(contentStream, page);
            }

            document.save(outputPath);
            LOGGER.info("Reporte estadístico generado: " + outputPath);
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar reporte estadístico", e);
            return false;
        }
    }

    // Métodos auxiliares privados

    private static float addHeader(PDPageContentStream contentStream, PDPage page,
                                   String title, float yPosition) throws IOException {
        // Título principal
        contentStream.beginText();
        contentStream.setFont(FONT_TITLE, FONT_SIZE_TITLE);
        float titleWidth = FONT_TITLE.getStringWidth(title) / 1000 * FONT_SIZE_TITLE;
        float titleX = (page.getMediaBox().getWidth() - titleWidth) / 2;
        contentStream.newLineAtOffset(titleX, yPosition);
        contentStream.showText(title);
        contentStream.endText();

        // Línea separadora
        yPosition -= PARAGRAPH_SPACING;
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, yPosition);
        contentStream.stroke();

        return yPosition - LINE_SPACING;
    }

    private static float addInstitutionalHeader(PDPageContentStream contentStream,
                                                PDPage page, float yPosition) throws IOException {
        String institucion = "SISTEMA DE CONTROL ESCOLAR";
        String direccion = "Av. Educación #123, Col. Académica";
        String contacto = "Tel: (55) 1234-5678 | Email: info@controlescolar.edu.mx";

        contentStream.beginText();
        contentStream.setFont(FONT_SUBTITLE, FONT_SIZE_SUBTITLE);
        float titleWidth = FONT_SUBTITLE.getStringWidth(institucion) / 1000 * FONT_SIZE_SUBTITLE;
        float titleX = (page.getMediaBox().getWidth() - titleWidth) / 2;
        contentStream.newLineAtOffset(titleX, yPosition);
        contentStream.showText(institucion);
        contentStream.endText();
        yPosition -= LINE_SPACING;

        contentStream.beginText();
        contentStream.setFont(FONT_SMALL, FONT_SIZE_SMALL);
        float dirWidth = FONT_SMALL.getStringWidth(direccion) / 1000 * FONT_SIZE_SMALL;
        float dirX = (page.getMediaBox().getWidth() - dirWidth) / 2;
        contentStream.newLineAtOffset(dirX, yPosition);
        contentStream.showText(direccion);
        contentStream.endText();
        yPosition -= LINE_SPACING;

        contentStream.beginText();
        contentStream.setFont(FONT_SMALL, FONT_SIZE_SMALL);
        float contactWidth = FONT_SMALL.getStringWidth(contacto) / 1000 * FONT_SIZE_SMALL;
        float contactX = (page.getMediaBox().getWidth() - contactWidth) / 2;
        contentStream.newLineAtOffset(contactX, yPosition);
        contentStream.showText(contacto);
        contentStream.endText();

        return yPosition;
    }

    private static float addStudentInfo(PDPageContentStream contentStream,
                                        Alumno alumno, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(FONT_SUBTITLE, FONT_SIZE_SUBTITLE);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("INFORMACIÓN DEL ALUMNO");
        contentStream.endText();
        yPosition -= LINE_SPACING;

        String[] infoLines = {
                "Nombre: " + alumno.getNombre() + " " + alumno.getApellidos(),
                "Número de Control: " + alumno.getNumeroControl(),
                "Email: " + alumno.getEmail(),
                "Fecha de generación: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        };

        for (String line : infoLines) {
            contentStream.beginText();
            contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(line);
            contentStream.endText();
            yPosition -= LINE_SPACING;
        }

        return yPosition;
    }

    private static float addGradesTable(PDPageContentStream contentStream,
                                        List<Calificacion> calificaciones, float yPosition) throws IOException {
        // Encabezado de tabla
        contentStream.beginText();
        contentStream.setFont(FONT_SUBTITLE, FONT_SIZE_SUBTITLE);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("CALIFICACIONES");
        contentStream.endText();
        yPosition -= PARAGRAPH_SPACING;

        // Headers de la tabla
        String[] headers = {"Materia", "Parcial", "Calificación", "Fecha"};
        float[] columnWidths = {200f, 80f, 100f, 100f};
        float tableX = MARGIN;

        // Dibujar headers
        contentStream.beginText();
        contentStream.setFont(FONT_SUBTITLE, FONT_SIZE_NORMAL);
        float currentX = tableX;
        for (int i = 0; i < headers.length; i++) {
            contentStream.newLineAtOffset(currentX, yPosition);
            contentStream.showText(headers[i]);
            contentStream.newLineAtOffset(-currentX, 0);
            currentX += columnWidths[i];
        }
        contentStream.endText();
        yPosition -= LINE_SPACING;

        // Línea separadora
        contentStream.moveTo(tableX, yPosition);
        contentStream.lineTo(tableX + 480, yPosition);
        contentStream.stroke();
        yPosition -= LINE_SPACING;

        // Datos de la tabla
        for (Calificacion cal : calificaciones) {
            contentStream.beginText();
            contentStream.setFont(FONT_NORMAL, FONT_SIZE_SMALL);
            currentX = tableX;

            String[] rowData = {
                    cal.getMateriaId().toString(),
                    cal.getTipo().toString(),
                    String.valueOf(cal.getCalificacion()),
                    cal.getFechaRegistro().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            };

            for (int i = 0; i < rowData.length; i++) {
                contentStream.newLineAtOffset(currentX, yPosition);
                contentStream.showText(rowData[i]);
                contentStream.newLineAtOffset(-currentX, 0);
                currentX += columnWidths[i];
            }
            contentStream.endText();
            yPosition -= LINE_SPACING;
        }

        return yPosition;
    }

    private static float addAttendanceTable(PDPageContentStream contentStream,
                                            List<Alumno> alumnos, float yPosition, PDPage page) throws IOException {
        // Encabezado de tabla
        String[] headers = {"No.", "Nombre Completo", "Número de Control", "Asistencia"};
        float[] columnWidths = {40f, 250f, 120f, 80f};
        float tableX = MARGIN;

        // Dibujar headers
        contentStream.beginText();
        contentStream.setFont(FONT_SUBTITLE, FONT_SIZE_NORMAL);
        float currentX = tableX;
        for (int i = 0; i < headers.length; i++) {
            contentStream.newLineAtOffset(currentX, yPosition);
            contentStream.showText(headers[i]);
            contentStream.newLineAtOffset(-currentX, 0);
            currentX += columnWidths[i];
        }
        contentStream.endText();
        yPosition -= LINE_SPACING;

        // Línea separadora
        contentStream.moveTo(tableX, yPosition);
        contentStream.lineTo(tableX + 490, yPosition);
        contentStream.stroke();
        yPosition -= LINE_SPACING;

        // Datos de la tabla
        int index = 1;
        for (Alumno alumno : alumnos) {
            contentStream.beginText();
            contentStream.setFont(FONT_NORMAL, FONT_SIZE_SMALL);
            currentX = tableX;

            String[] rowData = {
                    String.valueOf(index),
                    alumno.getNombre() + " " + alumno.getApellidos(),
                    alumno.getNumeroControl(),
                    "[ ]" // Checkbox para marcar asistencia
            };

            for (int i = 0; i < rowData.length; i++) {
                contentStream.newLineAtOffset(currentX, yPosition);
                contentStream.showText(rowData[i]);
                contentStream.newLineAtOffset(-currentX, 0);
                currentX += columnWidths[i];
            }
            contentStream.endText();
            yPosition -= LINE_SPACING * 1.5f;
            index++;
        }

        return yPosition;
    }

    private static float addPaymentsTable(PDPageContentStream contentStream,
                                          List<Pago> pagos, float yPosition) throws IOException {
        // Título de la tabla
        contentStream.beginText();
        contentStream.setFont(FONT_SUBTITLE, FONT_SIZE_SUBTITLE);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("HISTORIAL DE PAGOS");
        contentStream.endText();
        yPosition -= PARAGRAPH_SPACING;

        // Headers
        String[] headers = {"Concepto", "Monto", "Fecha Pago", "Estado"};
        float[] columnWidths = {200f, 100f, 100f, 100f};
        float tableX = MARGIN;

        contentStream.beginText();
        contentStream.setFont(FONT_SUBTITLE, FONT_SIZE_NORMAL);
        float currentX = tableX;
        for (int i = 0; i < headers.length; i++) {
            contentStream.newLineAtOffset(currentX, yPosition);
            contentStream.showText(headers[i]);
            contentStream.newLineAtOffset(-currentX, 0);
            currentX += columnWidths[i];
        }
        contentStream.endText();
        yPosition -= LINE_SPACING;

        // Línea separadora
        contentStream.moveTo(tableX, yPosition);
        contentStream.lineTo(tableX + 500, yPosition);
        contentStream.stroke();
        yPosition -= LINE_SPACING;

        // Verificar si hay pagos
        if (pagos == null || pagos.isEmpty()) {
            contentStream.beginText();
            contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
            contentStream.newLineAtOffset(tableX, yPosition);
            contentStream.showText("No hay pagos registrados");
            contentStream.endText();
            return yPosition - LINE_SPACING;
        }

        // Datos
        double totalPagado = 0;
        double totalPendiente = 0;
        
        for (Pago pago : pagos) {
            try {
                contentStream.beginText();
                contentStream.setFont(FONT_NORMAL, FONT_SIZE_SMALL);
                currentX = tableX;

                // Obtener datos de forma segura
                String concepto = pago.getConcepto() != null ? pago.getConcepto() : 
                                 (pago.getPeriodo() != null ? pago.getPeriodo() : "Sin concepto");
                String monto = String.format("$%.2f", pago.getMontoTotal());
                String fechaPago = pago.getFechaPago() != null ?
                        pago.getFechaPago().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Pendiente";
                String estado = pago.getEstado() != null ? pago.getEstado().getNombre() : "Sin estado";

                String[] rowData = {concepto, monto, fechaPago, estado};

                for (int i = 0; i < rowData.length; i++) {
                    contentStream.newLineAtOffset(currentX, yPosition);
                    contentStream.showText(rowData[i] != null ? rowData[i] : "");
                    contentStream.newLineAtOffset(-currentX, 0);
                    currentX += columnWidths[i];
                }
                contentStream.endText();
                yPosition -= LINE_SPACING;

                // Calcular totales
                if (pago.getFechaPago() != null && pago.getEstado() != null &&
                    (pago.getEstado().isCompletado())) {
                    totalPagado += pago.getMontoPagado();
                } else {
                    totalPendiente += pago.getSaldoPendiente();
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error al procesar pago en tabla: " + e.getMessage());
                // Continuar con el siguiente pago
            }
        }

        // Resumen financiero
        yPosition -= LINE_SPACING;
        contentStream.beginText();
        contentStream.setFont(FONT_SUBTITLE, FONT_SIZE_NORMAL);
        contentStream.newLineAtOffset(MARGIN + 200, yPosition);
        contentStream.showText(String.format("Total Pagado: $%.2f", totalPagado));
        contentStream.endText();
        
        yPosition -= LINE_SPACING;
        contentStream.beginText();
        contentStream.setFont(FONT_SUBTITLE, FONT_SIZE_NORMAL);
        contentStream.newLineAtOffset(MARGIN + 200, yPosition);
        contentStream.showText(String.format("Total Pendiente: $%.2f", totalPendiente));
        contentStream.endText();

        return yPosition;
    }

    private static float addStatistics(PDPageContentStream contentStream,
                                       Map<String, Object> estadisticas, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(FONT_SUBTITLE, FONT_SIZE_SUBTITLE);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("ESTADÍSTICAS GENERALES");
        contentStream.endText();
        yPosition -= PARAGRAPH_SPACING;

        for (Map.Entry<String, Object> entry : estadisticas.entrySet()) {
            if (!entry.getKey().equals("periodo")) {
                contentStream.beginText();
                contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText(entry.getKey() + ": " + entry.getValue().toString());
                contentStream.endText();
                yPosition -= LINE_SPACING;
            }
        }

        return yPosition;
    }

    private static float addWrappedText(PDPageContentStream contentStream, String text,
                                        float x, float y, float width, PDType1Font font,
                                        float fontSize) throws IOException {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float currentY = y;

        for (String word : words) {
            String testLine = line.length() > 0 ? line + " " + word : word;
            float testWidth = font.getStringWidth(testLine) / 1000 * fontSize;

            if (testWidth > width && line.length() > 0) {
                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.newLineAtOffset(x, currentY);
                contentStream.showText(line.toString());
                contentStream.endText();

                line = new StringBuilder(word);
                currentY -= LINE_SPACING;
            } else {
                line = new StringBuilder(testLine);
            }
        }

        if (line.length() > 0) {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(x, currentY);
            contentStream.showText(line.toString());
            contentStream.endText();
            currentY -= LINE_SPACING;
        }

        return currentY;
    }

    private static void addFooter(PDPageContentStream contentStream, PDPage page) throws IOException {
        float footerY = MARGIN;
        String footerText = "Documento generado el " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        contentStream.beginText();
        contentStream.setFont(FONT_SMALL, FONT_SIZE_SMALL);
        float footerWidth = FONT_SMALL.getStringWidth(footerText) / 1000 * FONT_SIZE_SMALL;
        float footerX = (page.getMediaBox().getWidth() - footerWidth) / 2;
        contentStream.newLineAtOffset(footerX, footerY);
        contentStream.showText(footerText);
        contentStream.endText();
    }

    /**
     * Valida que el directorio de salida exista
     */
    private static void validateOutputDirectory(String outputPath) {
        File file = new File(outputPath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
    }

    /**
     * Genera recibo de pago
     */
    public static boolean generatePaymentReceipt(Pago pago, Alumno alumno, String outputPath) {
        PDDocument document = null;
        try {
            document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = page.getMediaBox().getHeight() - MARGIN;

                // Encabezado institucional
                yPosition = addInstitutionalHeader(contentStream, page, yPosition);
                yPosition -= PARAGRAPH_SPACING * 2;

                // Título del recibo
                contentStream.beginText();
                contentStream.setFont(FONT_TITLE, FONT_SIZE_TITLE);
                float titleWidth = FONT_TITLE.getStringWidth("RECIBO DE PAGO") / 1000 * FONT_SIZE_TITLE;
                float titleX = (page.getMediaBox().getWidth() - titleWidth) / 2;
                contentStream.newLineAtOffset(titleX, yPosition);
                contentStream.showText("RECIBO DE PAGO");
                contentStream.endText();
                yPosition -= PARAGRAPH_SPACING;

                // Folio
                contentStream.beginText();
                contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                contentStream.newLineAtOffset(page.getMediaBox().getWidth() - 150, yPosition);
                contentStream.showText("Folio: " + pago.getFolio());
                contentStream.endText();
                yPosition -= PARAGRAPH_SPACING * 2;

                // Información del alumno
                yPosition = addStudentInfo(contentStream, alumno, yPosition);
                yPosition -= PARAGRAPH_SPACING;

                // Detalles del pago
                yPosition = addPaymentDetails(contentStream, pago, yPosition);

                // Firma y sello
                yPosition -= PARAGRAPH_SPACING * 2;
                contentStream.beginText();
                contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                contentStream.newLineAtOffset(page.getMediaBox().getWidth() - 200, yPosition);
                contentStream.showText("_________________________");
                contentStream.endText();

                yPosition -= LINE_SPACING;
                contentStream.beginText();
                contentStream.setFont(FONT_SMALL, FONT_SIZE_SMALL);
                contentStream.newLineAtOffset(page.getMediaBox().getWidth() - 200, yPosition);
                contentStream.showText("Firma y Sello");
                contentStream.endText();

                addFooter(contentStream, page);
            }

            // Forzar liberación de memoria antes de guardar
            System.gc();
            
            document.save(outputPath);
            LOGGER.info("Recibo de pago generado: " + outputPath);
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar recibo de pago", e);
            return false;
        } finally {
            // Asegurar que el documento se cierre
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error al cerrar documento PDF de recibo", e);
                }
            }
        }
    }

    /**
     * Genera boleta de calificaciones
     */
    public static boolean generateGradeReportCard(Alumno alumno, Map<String, Object> historialAcademico, String outputPath) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = page.getMediaBox().getHeight() - MARGIN;

                // Encabezado
                yPosition = addInstitutionalHeader(contentStream, page, yPosition);
                yPosition -= PARAGRAPH_SPACING;

                // Título
                contentStream.beginText();
                contentStream.setFont(FONT_TITLE, FONT_SIZE_TITLE);
                float titleWidth = FONT_TITLE.getStringWidth("BOLETA DE CALIFICACIONES") / 1000 * FONT_SIZE_TITLE;
                float titleX = (page.getMediaBox().getWidth() - titleWidth) / 2;
                contentStream.newLineAtOffset(titleX, yPosition);
                contentStream.showText("BOLETA DE CALIFICACIONES");
                contentStream.endText();
                yPosition -= PARAGRAPH_SPACING * 2;

                // Información del alumno
                yPosition = addStudentInfo(contentStream, alumno, yPosition);
                yPosition -= PARAGRAPH_SPACING;

                // Tabla de calificaciones por materia
                yPosition = addGradeReportCardTable(contentStream, historialAcademico, yPosition);

                addFooter(contentStream, page);
            }

            document.save(outputPath);
            LOGGER.info("Boleta de calificaciones generada: " + outputPath);
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar boleta de calificaciones", e);
            return false;
        }
    }

    private static float addPaymentDetails(PDPageContentStream contentStream, Pago pago, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(FONT_SUBTITLE, FONT_SIZE_SUBTITLE);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("DETALLES DEL PAGO");
        contentStream.endText();
        yPosition -= PARAGRAPH_SPACING;

        String[] details = {
                "Período: " + pago.getPeriodo(),
                "Concepto: Colegiatura",
                "Monto Original: $" + String.format("%.2f", pago.getMontoOriginal()),
                "Recargos: $" + String.format("%.2f", pago.getMontoRecargo()),
                "Becas/Descuentos: $" + String.format("%.2f", pago.getMontoBeca()),
                "Total: $" + String.format("%.2f", pago.getMontoTotal()),
                "Monto Pagado: $" + String.format("%.2f", pago.getMontoPagado()),
                "Método de Pago: " + (pago.getMetodoPago() != null ? pago.getMetodoPago() : "N/A"),
                "Fecha de Pago: " + (pago.getFechaPago() != null ? 
                    pago.getFechaPago().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Pendiente"),
                "Estado: " + pago.getEstado().getNombre()
        };

        for (String detail : details) {
            contentStream.beginText();
            contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(detail);
            contentStream.endText();
            yPosition -= LINE_SPACING;
        }

        if (pago.getObservaciones() != null && !pago.getObservaciones().trim().isEmpty()) {
            yPosition -= LINE_SPACING;
            contentStream.beginText();
            contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Observaciones: " + pago.getObservaciones());
            contentStream.endText();
            yPosition -= LINE_SPACING;
        }

        return yPosition;
    }

    private static float addGradeReportCardTable(PDPageContentStream contentStream, 
                                                Map<String, Object> historial, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(FONT_SUBTITLE, FONT_SIZE_SUBTITLE);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("CALIFICACIONES POR PERÍODO");
        contentStream.endText();
        yPosition -= PARAGRAPH_SPACING;

        // Headers
        String[] headers = {"Materia", "Parcial 1", "Parcial 2", "Final", "Promedio", "Estado"};
        float[] columnWidths = {150f, 60f, 60f, 60f, 70f, 80f};
        float tableX = MARGIN;

        // Dibujar headers
        contentStream.beginText();
        contentStream.setFont(FONT_SUBTITLE, FONT_SIZE_SMALL);
        float currentX = tableX;
        for (int i = 0; i < headers.length; i++) {
            contentStream.newLineAtOffset(currentX, yPosition);
            contentStream.showText(headers[i]);
            contentStream.newLineAtOffset(-currentX, 0);
            currentX += columnWidths[i];
        }
        contentStream.endText();
        yPosition -= LINE_SPACING;

        // Línea separadora
        contentStream.moveTo(tableX, yPosition);
        contentStream.lineTo(tableX + 480, yPosition);
        contentStream.stroke();
        yPosition -= LINE_SPACING;

        // Aquí se agregarían los datos del historial académico
        // Por simplicidad, mostramos datos de ejemplo
        String[][] sampleData = {
                {"Matemáticas", "8.5", "9.0", "8.8", "8.8", "Aprobado"},
                {"Español", "9.0", "8.5", "9.2", "8.9", "Aprobado"},
                {"Historia", "7.5", "8.0", "7.8", "7.8", "Aprobado"},
                {"Ciencias", "9.5", "9.0", "9.3", "9.3", "Excelente"}
        };

        for (String[] rowData : sampleData) {
            contentStream.beginText();
            contentStream.setFont(FONT_NORMAL, FONT_SIZE_SMALL);
            currentX = tableX;

            for (int i = 0; i < rowData.length; i++) {
                contentStream.newLineAtOffset(currentX, yPosition);
                contentStream.showText(rowData[i]);
                contentStream.newLineAtOffset(-currentX, 0);
                currentX += columnWidths[i];
            }
            contentStream.endText();
            yPosition -= LINE_SPACING;
        }

        // Promedio general
        yPosition -= LINE_SPACING;
        contentStream.beginText();
        contentStream.setFont(FONT_SUBTITLE, FONT_SIZE_NORMAL);
        contentStream.newLineAtOffset(MARGIN + 300, yPosition);
        contentStream.showText("Promedio General: 8.7");
        contentStream.endText();

        return yPosition;
    }

    /**
     * Método genérico para generar reportes
     */
    public static boolean generarReporte(String titulo, javafx.collections.ObservableList<?> datos, String rutaArchivo) {
        try {
            validateOutputDirectory(rutaArchivo);
            
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    float yPosition = page.getMediaBox().getHeight() - MARGIN;

                    // Encabezado
                    yPosition = addHeader(contentStream, page, titulo, yPosition);
                    yPosition -= PARAGRAPH_SPACING;

                    // Datos (implementación básica)
                    contentStream.beginText();
                    contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                    contentStream.newLineAtOffset(MARGIN, yPosition);
                    contentStream.showText("Total de registros: " + datos.size());
                    contentStream.endText();
                    yPosition -= LINE_SPACING;

                    for (int i = 0; i < Math.min(datos.size(), 20); i++) {
                        contentStream.beginText();
                        contentStream.setFont(FONT_SMALL, FONT_SIZE_SMALL);
                        contentStream.newLineAtOffset(MARGIN, yPosition);
                        contentStream.showText((i + 1) + ". " + datos.get(i).toString());
                        contentStream.endText();
                        yPosition -= LINE_SPACING;
                    }

                    addFooter(contentStream, page);
                }

                document.save(rutaArchivo);
                LOGGER.info("Reporte genérico generado: " + rutaArchivo);
                return true;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar reporte genérico", e);
            return false;
        }
    }
}