package com.example.demo.service;

import com.example.demo.model.RecordStatus;
import com.example.demo.model.RecordType;
import com.example.demo.model.UserInput;
import com.example.demo.repository.RecordRepository;
import com.example.demo.repository.UserInputRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.demo.model.Record;

@Service
public class RecordService {
    private final UserInputRepository userInputRepository;
    private final RecordRepository recordRepository;
    public RecordService(RecordRepository recordRepository,
                         UserInputRepository userInputRepository) {
        this.recordRepository = recordRepository;
        this.userInputRepository = userInputRepository;
    }

    public List<Record> parseText(String text, Long projectId) {
        //MOCK inicial, (SUSTITUIR POR IA)
        Record record = new Record();
        record.setTitle("Registro generado (mock)");
        record.setDescription(text);
        record.setType(RecordType.AVANCE);
        record.setProjectId(projectId);

        Map<String, Object> structuredData = new HashMap<>();
        structuredData.put("concept", "general");

        record.setStructuredData(structuredData);
        return List.of(record);
    }
    public List<Record> confirmRecords(String rawText,
                                       Long projectId,
                                       String source,
                                       List<Record> records) {

        // 1. Crear input
        UserInput input = new UserInput();
        input.setRawText(rawText);
        input.setProjectId(projectId);
        input.setSource(source);

        UserInput savedInput = userInputRepository.save(input);

        // 2. Preparar records
        for (Record r : records) {

            r.setId(null);
            r.setSourceInput(savedInput);
            r.setProjectId(projectId);

            // opcional: valores por defecto
            if (r.getStatus() == null) {
                r.setStatus(RecordStatus.ABIERTA);
            }
        }

        // 3. Guardar
        return recordRepository.saveAll(records);
    }
}
