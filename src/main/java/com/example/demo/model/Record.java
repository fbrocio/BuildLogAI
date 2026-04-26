package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "records")
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordType type;

    @Enumerated(EnumType.STRING)
    private RecordStatus status;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "related_record_id")
    private Long relatedRecordId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt; //Lo gestiona la BD (DEFAULT CURRENT_TIMESTAMP)

    @ManyToOne
    @JoinColumn(name = "source_input_id")
    private UserInput sourceInput;

    @Column(name = "structured_data", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> structuredData;

    public Record() {}

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

    public Long getProjectId() {
        return projectId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public Long getRelatedRecordId() {
        return relatedRecordId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public UserInput getSourceInput() {
        return sourceInput;
    }

    public Map<String, Object> getStructuredData() {
        return structuredData;
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

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public void setRelatedRecordId(Long relatedRecordId) {
        this.relatedRecordId = relatedRecordId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public void setSourceInput(UserInput sourceInput) {
        this.sourceInput = sourceInput;
    }

    public void setStructuredData(Map<String, Object> structuredData) {
        this.structuredData = structuredData;
    }
}