package com.example.demo.service;

import com.example.demo.model.Record;
import com.example.demo.repository.RecordRepository;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private final RecordRepository recordRepository;
    private final ProjectAccessService projectAccessService;
    private final AIService aiService;

    public ReportService(
            RecordRepository recordRepository,
            ProjectAccessService projectAccessService,
            AIService aiService
    ) {
        this.recordRepository = recordRepository;
        this.projectAccessService = projectAccessService;
        this.aiService = aiService;
    }

    public String generateReport(String query, Long projectId, Long userId) {

        projectAccessService.requireMember(projectId, userId);

        List<Record> records;

        if (isDate(query)) {

            LocalDate date = LocalDate.parse(
                    query,
                    DateTimeFormatter.ofPattern("dd-MM-yyyy")
            );

            LocalDateTime start = date.atStartOfDay();

            LocalDateTime end = date.atTime(23, 59, 59);

            records = recordRepository.findByProject_IdAndCreatedAtBetween(
                    projectId,
                    start,
                    end
            );

        } else {

            records = recordRepository
                    .findByProject_IdAndTitleContainingIgnoreCase(projectId, query);
        }

        String markdown = buildMarkdown(records);

        return aiService.generateReport(query, markdown);
    }

    private boolean isDate(String value) {

        try {

            LocalDate.parse(
                    value,
                    DateTimeFormatter.ofPattern("dd-MM-yyyy")
            );

            return true;

        } catch (Exception e) {

            return false;
        }
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
