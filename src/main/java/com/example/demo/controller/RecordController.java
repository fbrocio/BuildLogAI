package com.example.demo.controller;

import com.example.demo.dto.ConfirmRequest;
import com.example.demo.dto.ImageResponse;
import com.example.demo.dto.ParseRequest;
import com.example.demo.dto.RecordDTO;
import com.example.demo.dto.RecordStatusUpdateRequest;
import com.example.demo.service.AIService;
import com.example.demo.service.RecordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/records")
public class RecordController {

    private final RecordService recordService;
    private final AIService aiService;

    public RecordController(
            RecordService recordService,
            AIService aiService) {

        this.recordService = recordService;
        this.aiService = aiService;
    }

    @PostMapping
    public ResponseEntity<RecordDTO> crear(
            @Valid @RequestBody RecordDTO record,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");

        com.example.demo.model.Record savedRecord =
                recordService.createRecord(record, userId);

        return ResponseEntity.ok(new RecordDTO(savedRecord));
    }

    @GetMapping("/project/{projectId}")
    public List<RecordDTO> getByProject(
            @PathVariable Long projectId,
            HttpServletRequest httpRequest) {

        Long userId =
                (Long) httpRequest.getAttribute("userId");

        return recordService.getRecordsByProject(
                projectId,
                userId
        );
    }

    @PostMapping("/parse")
    public ResponseEntity<?> parse(
            @Valid @RequestBody ParseRequest request,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        recordService.verifyProjectAccess(request.getProjectId(), userId);

        List<RecordDTO> records =
                aiService.generateRecordsFromText(
                        request.getText(),
                        request.getProjectId()
                ).stream().map(RecordDTO::new).toList();

        return ResponseEntity.ok(
                Map.of("records", records)
        );
    }

    @PostMapping("/confirm")
    public ResponseEntity<List<RecordDTO>> confirm(
            @Valid @RequestBody ConfirmRequest request,
            HttpServletRequest httpRequest) {

        List<RecordDTO> savedRecords =
                recordService.confirmRecords(
                        request,
                        (Long) httpRequest.getAttribute("userId")
                );

        return ResponseEntity.ok(savedRecords);
    }

    @GetMapping
    public List<RecordDTO> getAll(HttpServletRequest httpRequest) {
        return recordService.getAllRecords(
                (Long) httpRequest.getAttribute("userId")
        );
    }

    @GetMapping("/{id}")
    public RecordDTO getById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        return recordService.getRecordById(
                id,
                (Long) httpRequest.getAttribute("userId")
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        Long userId =
                (Long) httpRequest.getAttribute("userId");

        recordService.deleteRecord(id, userId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecordDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody RecordDTO newRecord,
            HttpServletRequest httpRequest) {

        Long userId =
                (Long) httpRequest.getAttribute("userId");

        RecordDTO updated =
                recordService.updateRecord(
                        id,
                        newRecord,
                        userId
                );

        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RecordDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody RecordStatusUpdateRequest request,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(recordService.updateStatus(
                id,
                request.getStatus(),
                (Long) httpRequest.getAttribute("userId")
        ));
    }

    // IMAGES

    @GetMapping("/{id}/images")
    public ResponseEntity<List<ImageResponse>> getImages(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(
                recordService.getRecordImages(
                        id,
                        (Long) httpRequest.getAttribute("userId")
                ).stream().map(image -> new ImageResponse(image.getId(), image.getImageUrl())).toList()
        );
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ImageResponse> addImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image,
            HttpServletRequest httpRequest) {

        Long userId =
                (Long) httpRequest.getAttribute("userId");

        ImageResponse response =
                recordService.addImage(id, image, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long imageId,
            HttpServletRequest httpRequest) {

        Long userId =
                (Long) httpRequest.getAttribute("userId");

        recordService.deleteImage(imageId, userId);

        return ResponseEntity.noContent().build();
    }
}
