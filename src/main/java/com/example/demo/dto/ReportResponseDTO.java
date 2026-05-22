package com.example.demo.dto;

public class ReportResponseDTO {

    private String summary;

    public ReportResponseDTO(String summary) {
        this.summary = summary;
    }

    public String getSummary() {
        return summary;
    }
}
