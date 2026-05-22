package com.example.demo.service;

import com.example.demo.model.Record;
import com.example.demo.repository.RecordRepository;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class ReportService {

    private final RecordRepository recordRepository;
    private final AIService aiService;

    public ReportService(
            RecordRepository recordRepository,
            AIService aiService
    ) {
        this.recordRepository = recordRepository;
        this.aiService = aiService;
    }

    public String generateReport(String topic) {

        List<Record> records =
                recordRepository
                        .findByTitleContainingIgnoreCase(topic);

        String markdown = buildMarkdown(records);

        return aiService.generateReport(topic, markdown);
    }

    private String buildMarkdown(List<Record> records) {

        StringBuilder md = new StringBuilder();

        md.append("# Registros de obra\n\n");

        for (Record r : records) {

            md.append("## ")
                    .append(r.getTitle())
                    .append("\n\n");

            md.append("Fecha: ")
                    .append(r.getCreatedAt())
                    .append("\n\n");

            if (r.getDescription() != null) {

                md.append(r.getDescription())
                        .append("\n\n");
            }

            if (r.getCreatedBy() != null) {

                md.append("Autor: ")
                        .append(r.getCreatedBy().getName())
                        .append("\n\n");
            }

            md.append("---\n\n");
        }

        return md.toString();
    }
}