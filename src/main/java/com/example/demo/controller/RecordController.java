package com.example.demo.controller;

import com.example.demo.dto.ConfirmRequest;
import com.example.demo.dto.ImageResponse;
import com.example.demo.dto.ParseRequest;
import com.example.demo.dto.RecordDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Project;
import com.example.demo.model.Record;
import com.example.demo.model.RecordImage;
import com.example.demo.model.User;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.RecordImageRepository;
import com.example.demo.repository.RecordRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AIService;
import com.example.demo.service.CloudinaryService;
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
import org.springframework.web.server.ResponseStatusException;

import java.awt.*;
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
    private final CloudinaryService cloudinaryService;
    private final ProjectRepository projectRepository;

    public RecordController(
            RecordRepository repository,
            RecordService recordService,
            AIService AIService,
            RecordImageRepository recordImageRepository,
            FileStorageService fileStorageService,
            UserRepository userRepository,
            CloudinaryService cloudinaryService,
            ProjectRepository projectRepository) {
        this.repository = repository;
        this.recordService = recordService;
        this.AIService = AIService;
        this.recordImageRepository = recordImageRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
        this.projectRepository = projectRepository;
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
    public List<RecordDTO> getByProject(@PathVariable Long projectId, HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException(
                    "No autenticado"
            );
        }

        Project project = projectRepository.findById(projectId).orElseThrow(() ->
                new ResourceNotFoundException("Project not found"));

        verifyProjectMember(project, userId);

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

    @PostMapping("/{id}/images")
    public ResponseEntity<?> addImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image,
            HttpServletRequest httpRequest

    ) {

        Long userId =
                (Long) httpRequest.getAttribute("userId");

        if (userId == null) {

            throw new RuntimeException(
                    "No autenticado"
            );
        }

        try {

            // Buscar record

            Record record = repository.findById(id)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Record not found"
                            ));

            verifyRecordOwner(record, userId);

            if (image.isEmpty()) {
                throw new RuntimeException("Image is empty");
            }

            String contentType = image.getContentType();

            if (contentType == null ||
                    !contentType.startsWith("image/")) {

                throw new RuntimeException("Invalid file type");
            }

            if (image.getSize() > 10 * 1024 * 1024) {
                throw new RuntimeException("File too large");
            }

            System.out.println("RECORD OK");

            // Subir imagen a Cloudinary

            String imageUrl =
                    cloudinaryService.uploadFile(image);

            System.out.println("IMAGE UPLOADED: " + imageUrl);

            // Crear entidad

            RecordImage recordImage = new RecordImage();

            recordImage.setRecord(record);

            recordImage.setImageUrl(imageUrl);

            System.out.println("ENTITY CREATED");

            // Guardar en PostgreSQL

            RecordImage savedImage =
                    recordImageRepository.save(recordImage);

            System.out.println("DB SAVED");

            ImageResponse response = new ImageResponse(
                    savedImage.getId(),
                    savedImage.getImageUrl()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

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
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {

        Long userId =
                (Long) httpRequest.getAttribute("userId");

        if (userId == null) {

            throw new RuntimeException(
                    "No autenticado"
            );
        }

        Record record = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Record not found"
                        )
                );

        verifyRecordOwner(record, userId);

        repository.delete(record);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody Record newRecord,
                                    BindingResult result,
                                    HttpServletRequest httpRequest
    ){
        //1. Validación
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }

        //Usuario autenticado
        Long userId =
                (Long) httpRequest.getAttribute("userId");

        if (userId == null) {

            throw new RuntimeException(
                    "No autenticado"
            );
        }
        //2. Buscar existente (o devolver 404)
        Record existing = repository.findById(id)
                        .orElseThrow(()-> new ResourceNotFoundException("Record not found"));

        // Verificar propietario
        verifyRecordOwner(existing, userId);

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

        System.out.println(
                "IMAGES COUNT: "
                        + record.getImages().size()
        );

        for (RecordImage image : record.getImages()) {
            System.out.println(
                    "IMAGE ID: " + image.getId()
            );
        }

        return ResponseEntity.ok(
                record.getImages()
        );
    }
    /*@GetMapping("/{id}/images")
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
    }*/
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long imageId,
            HttpServletRequest httpRequest
    ) {

        Long userId =
                (Long) httpRequest.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("No autenticado");
        }

        RecordImage image = recordImageRepository.findById(imageId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Image not found"
                        ));

        Record record = image.getRecord();

        verifyRecordOwner(record, userId);

        record.getImages().remove(image);

        System.out.println(
                "IMAGES AFTER DELETE: "
                        + record.getImages().size()
        );

        repository.save(record);

        return ResponseEntity.noContent().build();
    }

    private void verifyRecordOwner(
            Record record,
            Long userId
    ) {

        if (record.getCreatedBy() == null ||
                !record.getCreatedBy()
                        .getId()
                        .equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo el creador puede modificar este registro"
            );
        }
    }

    private void verifyProjectMember(
            Project project,
            Long userId
    ) {

        boolean isMember = project.getUsers()
                .stream()
                .anyMatch(user ->
                        user.getId().equals(userId)
                );

        if (!isMember) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes acceso a este proyecto"
            );
        }
    }

}
