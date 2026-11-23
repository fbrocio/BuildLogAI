package com.example.demo.controller;

import com.example.demo.dto.ReportRequestDTO;
import com.example.demo.dto.ReportResponseDTO;
import com.example.demo.service.PdfService;
import com.example.demo.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final PdfService pdfService;

    public ReportController(
            ReportService reportService,
            PdfService pdfService
    ) {
        this.reportService = reportService;
        this.pdfService = pdfService;
    }

    @PostMapping("/generate")
    public ReportResponseDTO generate(
            @Valid @RequestBody ReportRequestDTO request,
            HttpServletRequest httpRequest
    ) {

        String report = reportService.generateReport(
                request.getTopic(),
                request.getProjectId(),
                (Long) httpRequest.getAttribute("userId")
        );

        return new ReportResponseDTO(report);
    }

    @PostMapping(
            value = "/generate-pdf",
            produces = "application/pdf"
    )
    public ResponseEntity<byte[]> generatePdf(
            @Valid @RequestBody ReportRequestDTO request,
            HttpServletRequest httpRequest
    ) {

        String report =
                reportService.generateReport(
                        request.getTopic(),
                        request.getProjectId(),
                        (Long) httpRequest.getAttribute("userId")
                );

        byte[] pdf = pdfService.generatePdf(
                "Informe de obra",
                report
        );

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_PDF);

        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename(
                                "informe_" + System.currentTimeMillis() + ".pdf"
                        )
                        .build()
        );

        return new ResponseEntity<>(
                pdf,
                headers,
                HttpStatus.OK
        );
    }
}
