package com.example.demo.dto;

import java.util.List;

public class AIResponse {
    private List<RecordDTO> records;

    public List<RecordDTO> getRecords() {
        return records;
    }

    public void setRecords(List<RecordDTO> records) {
        this.records = records;
    }
}
