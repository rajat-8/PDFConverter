package com.Converter.Service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.*;
import org.springframework.stereotype.Service;

@Service
public class PdfGeneratorService {

    public void generatePdfFromHtml(String htmlContent) throws IOException {
        String outputPath = "ITN-WBD.pdf";
        File pdfFile = new File(outputPath);
        pdfFile.createNewFile();
        try (OutputStream outputStream = new FileOutputStream(pdfFile, false)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null); // Base URI can be set for loading images, CSS, etc.
            builder.toStream(outputStream);
            builder.run();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error generating PDF", e);
        }
    }
}
