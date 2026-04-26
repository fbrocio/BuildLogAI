package com.example.demo.dto;

import java.util.List;
import com.example.demo.model.Record;

public class ParseResponse {
    private List<Record> records;

    public ParseResponse(List<Record> records) {
        this.records = records;
    }

    public List<Record> getRecords() {
        return records;
    }
}
