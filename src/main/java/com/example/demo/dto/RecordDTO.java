package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RecordDTO {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @Pattern(regexp = "PENDIENTE|INCIDENCIA|AVANCE")
    private String type;
    @Pattern(regexp = "ABIERTA|CERRADA")
    private String status;
    private StructuredData structuredData;
    private Long projectId;

    public RecordDTO(){}

    public RecordDTO(
            String title,
            String description,
            String type,
            String status,
            StructuredData structuredData,
            Long projectId) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.status = status;
        this.structuredData = structuredData;
        this.projectId = projectId;

    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public StructuredData getStructuredData(){
        return structuredData;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public void setStructuredData(StructuredData structuredData){
        this.structuredData = structuredData;
    }
}
