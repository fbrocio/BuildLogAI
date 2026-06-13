package com.example.demo.service;

import com.example.demo.dto.RecordDTO;
import com.example.demo.dto.UserResponse;
import com.example.demo.model.*;
import com.example.demo.model.Record;
import com.example.demo.repository.RecordRepository;
import com.example.demo.repository.UserInputRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecordService {
    private final UserInputRepository userInputRepository;
    private final RecordRepository recordRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    public RecordService(RecordRepository recordRepository,
                         UserInputRepository userInputRepository,
                         UserRepository userRepository,
                         ObjectMapper objectMapper) {
        this.recordRepository = recordRepository;
        this.userInputRepository = userInputRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public List<Record> confirmRecords(String rawText,
                                       Long projectId,
                                       String source,
                                       List<Record> records,
                                       Long userId) {

        // 1. Crear input
        UserInput input = new UserInput();
        input.setRawText(rawText);
        input.setProjectId(projectId);
        input.setSource(source);

        UserInput savedInput = userInputRepository.save(input);
        User currentUser = userRepository.findById(userId).orElseThrow();

        // 2. Preparar records
        for (Record r : records) {

            r.setId(null);
            r.setSourceInput(savedInput);
            r.setProjectId(projectId);
            r.setCreatedBy(currentUser);
            r.setCreatedAt(LocalDateTime.now());

            // opcional: valores por defecto
            if (r.getStatus() == null) {
                r.setStatus(RecordStatus.ABIERTA);
            }
        }

        // 3. Guardar
        return recordRepository.saveAll(records);
    }

    private RecordDTO mapToDTO(Record record) {

        RecordDTO dto = new RecordDTO();

        dto.setId(record.getId());

        dto.setTitle(record.getTitle());

        dto.setDescription(record.getDescription());

        dto.setType(record.getType());

        dto.setStatus(record.getStatus());

        dto.setProjectId(record.getProjectId());

        dto.setStructuredData(record.getStructuredData());

        dto.setCreatedAt(record.getCreatedAt());

        User user = record.getCreatedBy();

        if (user != null) {

            dto.setCreatedBy(
                    new UserResponse(user)
            );
        }

        return dto;
    }

    public List<RecordDTO> getRecordsByProject(Long projectId) {
        List<Record> records = recordRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        return records.stream().map(this::mapToDTO).toList();
    }

    public RecordDTO updateStatus(
            Long recordId,
            String status
    ) {

        Record record = recordRepository
                .findById(recordId)
                .orElseThrow();

        record.setStatus(
                RecordStatus.valueOf(status)
        );

        Record updatedRecord = recordRepository.save(record);

        return mapToDTO(updatedRecord);
    }
}
