package com.example.demo.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PdfServiceTest {

    @Test
    void generatePdfRendersMarkdownContent() {
        PdfService pdfService = new PdfService();

        byte[] pdf = pdfService.generatePdf(
                "Informe de obra",
                """
                        # Informe de obra

                        ## Resumen ejecutivo

                        Texto con **negrita** y una lista:

                        - Punto uno
                        - Punto dos

                        | Campo | Valor |
                        | --- | --- |
                        | Estado | Abierto |
                        """
        );

        String header = new String(
                pdf,
                0,
                Math.min(pdf.length, 4),
                StandardCharsets.US_ASCII
        );

        assertThat(header).isEqualTo("%PDF");
        assertThat(pdf).hasSizeGreaterThan(1_000);
    }
}
