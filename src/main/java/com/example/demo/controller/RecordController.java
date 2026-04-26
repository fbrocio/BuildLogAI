package com.example.demo.controller;

import com.example.demo.dto.ConfirmRequest;
import com.example.demo.dto.ParseRequest;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Record;
import com.example.demo.repository.RecordRepository;
import com.example.demo.service.AIService;
import com.example.demo.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * La clase RecordController se comunica con la base de datos
 */
@RestController
@RequestMapping("/records")
public class RecordController {

    @Autowired
    private final RecordRepository repository;
    private final RecordService recordService;
    private final AIService AIService;

    public RecordController(RecordRepository repository, RecordService recordService, AIService AIService) {
        this.repository = repository;
        this.recordService = recordService;
        this.AIService = AIService;
    }

    @PostMapping
    /*
     * @ResponseEntity permite controlar código HTTP y respuesta
     * @RequestBody convierte JSON a objeto java
     * @BindingResult detecta si hay errores de validación
     */
    public ResponseEntity<?> crear(@Valid @RequestBody Record record, BindingResult result) {

        System.out.println("RECIBIDO: " + record.getTitle());

        //Manejo de errores
        if(result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()){
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity
                    .badRequest()
                    .body(errors);
        }

        //Guarda la entidad en BD y devuelve el objeto guardado
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(record));
    }
    @PostMapping("/parse")
    public ResponseEntity<?> parse(@RequestBody ParseRequest request) {

        List<Record> records = AIService.generateRecordsFromText(
                request.getText(),
                request.getProjectId()
        );

        return ResponseEntity.ok(Map.of("records", records));
    }

    @PostMapping("/confirm")
    public ResponseEntity<List<Record>> confirm(@RequestBody ConfirmRequest request) {

        // Validación mínima
        if (request.getRawText() == null || request.getRawText().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        if (request.getProjectId() == null) {
            return ResponseEntity.badRequest().build();
        }

        if (request.getRecords() == null || request.getRecords().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Record> savedRecords = recordService.confirmRecords(
                request.getRawText(),
                request.getProjectId(),
                request.getSource(),
                request.getRecords()
        );

        return ResponseEntity.ok(savedRecords);
    }

    @GetMapping
    public List<Record> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Record getById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        Record record = repository.findById(id)
                        .orElseThrow(()-> new ResourceNotFoundException("Record not found"));
        repository.delete(record);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody Record newRecord,
                                    BindingResult result){
        //1. Validación
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }
        //2. Buscar existente (o devolver 404)
        Record existing = repository.findById(id)
                        .orElseThrow(()-> new ResourceNotFoundException("Record not found"));

        //3. Actualizar campos permitidos
        existing.setTitle(newRecord.getTitle());
        existing.setDescription(newRecord.getDescription());
        existing.setType(newRecord.getType());
        existing.setStatus(newRecord.getStatus());
        existing.setProjectId(newRecord.getProjectId());

        //4. Guardar
        Record updated = repository.save(existing);

        //5. Respuesta
        return ResponseEntity.ok(updated);
    }
}