package com.Converter.Controller;

import com.Converter.Service.HtmlModifierService;
import com.Converter.Service.PdfGeneratorService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Autowired
    private HtmlModifierService htmlModifier;

    @Autowired
    private PdfGeneratorService pdfGenerator;

    @PostMapping("/generate")
    public ResponseEntity<?> generateVoucherPdf(@RequestBody String htmlContent) {

        try {
            // Modify the HTML
            String modifiedHtml = htmlModifier.modifyVoucherHtml(htmlContent);

            // Generate PDF
            pdfGenerator.generatePdfFromHtml(modifiedHtml);

            return ResponseEntity.ok("PDF generated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating PDF");
        }
    }

}
