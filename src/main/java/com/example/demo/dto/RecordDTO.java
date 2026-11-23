package com.example.demo.dto;

import com.example.demo.model.Record;
import com.example.demo.model.StructuredData;
import com.example.demo.model.RecordStatus;
import com.example.demo.model.RecordType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;

public class RecordDTO {

    private Long id;

    @NotBlank @Size(max = 255)
    private String title;

    @NotBlank @Size(max = 4000)
    private String description;

    @NotNull private RecordType type;

    @NotNull private RecordStatus status;

    private Map<String, Object> structuredData;

    @NotNull private Long projectId;

    private UserResponse createdBy;

    private LocalDateTime createdAt;

    public RecordDTO() {
    }

    public RecordDTO(
            Long id,
            String title,
            String description,
            RecordType type,
            RecordStatus status,
            Map<String, Object> structuredData,
            Long projectId,
            UserResponse createdBy,
            LocalDateTime createdAt
    ) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.status = status;
        this.structuredData = structuredData;
        this.projectId = projectId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public RecordDTO(Record record) {

        this.id = record.getId();
        this.title = record.getTitle();
        this.description = record.getDescription();

        this.type = record.getType();
        this.status = record.getStatus();

        this.structuredData = record.getStructuredData();

        this.projectId = record.getProject() == null ? null : record.getProject().getId();

        this.createdAt = record.getCreatedAt();

        if (record.getCreatedBy() != null) {

            this.createdBy = new UserResponse(record.getCreatedBy());
        }
    }

    // GETTERS

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public RecordType getType() {
        return type;
    }

    public RecordStatus getStatus() {
        return status;
    }

    public Map<String, Object> getStructuredData() {
        return structuredData;
    }

    public Long getProjectId() {
        return projectId;
    }

    public UserResponse getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // SETTERS

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(RecordType type) {
        this.type = type;
    }

    public void setStatus(RecordStatus status) {
        this.status = status;
    }

    public void setStructuredData(Map<String, Object> structuredData) {
        this.structuredData = structuredData;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public void setCreatedBy(UserResponse createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
