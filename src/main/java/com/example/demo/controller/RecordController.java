package com.example.demo.controller;

import com.example.demo.dto.ConfirmRequest;
import com.example.demo.dto.ParseRequest;
import com.example.demo.dto.RecordDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Record;
import com.example.demo.model.RecordImage;
import com.example.demo.model.User;
import com.example.demo.repository.RecordImageRepository;
import com.example.demo.repository.RecordRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AIService;
import com.example.demo.service.FileStorageService;
import com.example.demo.service.RecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

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
    private final RecordImageRepository recordImageRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    public RecordController(
            RecordRepository repository,
            RecordService recordService,
            AIService AIService,
            RecordImageRepository recordImageRepository,
            FileStorageService fileStorageService,
            UserRepository userRepository) {
        this.repository = repository;
        this.recordService = recordService;
        this.AIService = AIService;
        this.recordImageRepository = recordImageRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    @PostMapping
    /*
     * @ResponseEntity permite controlar código HTTP y respuesta
     * @RequestBody convierte JSON a objeto java
     * @BindingResult detecta si hay errores de validación
     */
    public ResponseEntity<?> crear(@Valid @RequestBody Record record,
                                   BindingResult result,
                                   HttpServletRequest httpRequest) {

        System.out.println("RECIBIDO: " + record.getTitle());
        System.out.println("====== RECORD RECIBIDO ======");
        System.out.println("Title: " + record.getTitle());
        System.out.println("ProjectId: " + record.getProjectId());
        System.out.println("Type: " + record.getType());
        System.out.println("Status: " + record.getStatus());

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

        Long userId = (Long) httpRequest.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("No autenticado");
        }

        User currentUser =
                userRepository.findById(userId)
                        .orElseThrow();

        record.setCreatedBy(currentUser);

        //Guarda la entidad en BD y devuelve el objeto guardado
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(record));

    }

    @GetMapping("/project/{projectId}")
    public List<RecordDTO> getByProject(@PathVariable Long projectId) {
        return recordService.getRecordsByProject(projectId);
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
    public ResponseEntity<List<Record>> confirm(@RequestBody ConfirmRequest request,
                                                HttpServletRequest httpRequest) {

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

        Long userId = (Long) httpRequest.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Record> savedRecords = recordService.confirmRecords(
                request.getRawText(),
                request.getProjectId(),
                request.getSource(),
                request.getRecords(),
                userId
        );

        return ResponseEntity.ok(savedRecords);
    }


    /*@PostMapping("/{id}/images")
    public ResponseEntity<?> addImage(
            @PathVariable Long id,
            @RequestParam("image")MultipartFile image
            ){
        //Buscar record
        Record record = repository.findById(id)
                .orElseThrow(()->
                        new ResourceNotFoundException("Record not found"));

        //Guardar archivo físico
        String imageUrl = fileStorageService.saveImage(image);

        //Crear entidad RecordImage
        RecordImage recordImage = new RecordImage();
        recordImage.setRecord(record);
        recordImage.setImageUrl(imageUrl);

        //Guardar en BD
        RecordImage savedImage =
                recordImageRepository.save(recordImage);

        //Respuesta
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedImage);
    }*/

    @PostMapping("/{id}/images")
    public ResponseEntity<?> addImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image
    ) {

        try {

            // Buscar record

            Record record = repository.findById(id)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Record not found"
                            ));

            System.out.println("RECORD OK");

            // Guardar archivo físico

            String imageUrl =
                    fileStorageService.saveImage(image);

            System.out.println("IMAGE SAVED: " + imageUrl);

            // Crear entidad

            RecordImage recordImage = new RecordImage();

            recordImage.setRecord(record);

            recordImage.setImageUrl(imageUrl);

            System.out.println("ENTITY CREATED");

            // Guardar BD

            RecordImage savedImage =
                    recordImageRepository.save(recordImage);

            System.out.println("DB SAVED");

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedImage);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @GetMapping
    public List<Record> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public RecordDTO getById(@PathVariable Long id) {

        Record record = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Record not found"));

        return new RecordDTO(record);
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

    @PatchMapping("/{id}/status")
    public RecordDTO updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {

        return recordService.updateStatus(
                id,
                body.get("status")
        );
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<RecordImage>> getImages(
            @PathVariable Long id
    ) {

        Record record = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Record not found"
                        ));

        return ResponseEntity.ok(
                record.getImages()
        );
    }
}
