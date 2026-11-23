package com.example.demo.service;

import com.example.demo.dto.ConfirmRequest;
import com.example.demo.dto.ImageResponse;
import com.example.demo.dto.RecordDTO;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.model.Record;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.RecordImageRepository;
import com.example.demo.repository.RecordRepository;
import com.example.demo.repository.UserInputRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecordService {

    private final UserInputRepository userInputRepository;
    private final RecordRepository recordRepository;
    private final RecordImageRepository recordImageRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAccessService projectAccessService;
    private final CloudinaryService cloudinaryService;

    public RecordService(
            RecordRepository recordRepository,
            RecordImageRepository recordImageRepository,
            UserInputRepository userInputRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            ProjectAccessService projectAccessService,
            CloudinaryService cloudinaryService) {

        this.recordRepository = recordRepository;
        this.recordImageRepository = recordImageRepository;
        this.userInputRepository = userInputRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectAccessService = projectAccessService;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public Record createRecord(RecordDTO dto, Long userId) {

        projectAccessService.requireMember(dto.getProjectId(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow();

        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Record record = new Record();

        record.setTitle(dto.getTitle());
        record.setDescription(dto.getDescription());
        record.setType(dto.getType());
        record.setStatus(dto.getStatus());
        record.setProject(project);
        record.setStructuredData(dto.getStructuredData());

        record.setCreatedBy(user);

        return recordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public List<RecordDTO> getRecordsByProject(
            Long projectId,
            Long userId) {

        projectAccessService.requireMember(projectId, userId);

        return recordRepository
                .findByProject_IdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public List<RecordDTO> confirmRecords(
            ConfirmRequest request,
            Long userId) {

        projectAccessService.requireMember(request.getProjectId(), userId);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Project project = projectRepository.findByIdAndUsers_Id(request.getProjectId(), userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "No tienes acceso a este proyecto"));

        UserInput input = new UserInput();

        input.setRawText(request.getRawText());
        input.setProjectId(request.getProjectId());
        input.setSource(request.getSource());

        UserInput savedInput =
                userInputRepository.save(input);

        List<Record> records = request.getRecords().stream().map(dto -> {
            Record record = new Record();
            record.setTitle(dto.getTitle());
            record.setDescription(dto.getDescription());
            record.setType(dto.getType());
            record.setStatus(dto.getStatus());
            record.setStructuredData(dto.getStructuredData());
            return record;
        }).toList();

        for (Record record : records) {

            record.setId(null);
            record.setSourceInput(savedInput);
            record.setProject(project);
            record.setCreatedBy(currentUser);
            record.setCreatedAt(LocalDateTime.now());

            if (record.getStatus() == null) {
                record.setStatus(RecordStatus.ABIERTA);
            }
        }

        return recordRepository.saveAll(records).stream().map(this::mapToDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<RecordDTO> getAllRecords(Long userId) {
        return projectRepository.findByUsers_Id(userId).stream()
                .flatMap(project -> recordRepository
                        .findByProject_IdOrderByCreatedAtDesc(project.getId())
                        .stream())
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecordDTO getRecordById(Long id, Long userId) {

        Record record = recordRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Record not found"
                        ));

        verifyRecordAccess(record, userId);

        return mapToDTO(record);
    }

    @Transactional
    public RecordDTO updateRecord(
            Long id,
            RecordDTO newData,
            Long userId) {

        Record existing = recordRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Record not found"
                        ));

        verifyRecordAccess(existing, userId);
        verifyRecordOwner(existing, userId);

        existing.setTitle(newData.getTitle());
        existing.setDescription(newData.getDescription());
        existing.setType(newData.getType());
        existing.setStatus(newData.getStatus());
        existing.setStructuredData(
                newData.getStructuredData()
        );

        Record updatedRecord =
                recordRepository.save(existing);

        return mapToDTO(updatedRecord);
    }

    @Transactional
    public void deleteRecord(
            Long id,
            Long userId) {

        Record record = recordRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Record not found"
                        ));

        verifyRecordAccess(record, userId);
        verifyRecordOwner(record, userId);

        recordRepository.delete(record);
    }

    @Transactional
    public ImageResponse addImage(
            Long recordId,
            MultipartFile image,
            Long userId) {

        Record record = recordRepository.findById(recordId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Record not found"
                        ));

        verifyRecordAccess(record, userId);
        verifyRecordOwner(record, userId);

        validateImage(image);

        String imageUrl =
                cloudinaryService.uploadFile(image);

        RecordImage recordImage = new RecordImage();

        recordImage.setRecord(record);
        recordImage.setImageUrl(imageUrl);

        RecordImage savedImage =
                recordImageRepository.save(recordImage);

        return new ImageResponse(
                savedImage.getId(),
                savedImage.getImageUrl()
        );
    }

    @Transactional(readOnly = true)
    public List<RecordImage> getRecordImages(
            Long recordId,
            Long userId) {

        Record record = recordRepository.findById(recordId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Record not found"
                        ));

        verifyRecordAccess(record, userId);

        return recordImageRepository.findByRecord_IdOrderByCreatedAtAsc(recordId);
    }

    @Transactional
    public void deleteImage(
            Long imageId,
            Long userId) {

        RecordImage image =
                recordImageRepository.findById(imageId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Image not found"
                                ));

        Record record = image.getRecord();

        verifyRecordAccess(record, userId);
        verifyRecordOwner(record, userId);

        recordImageRepository.delete(image);
    }

    @Transactional
    public RecordDTO updateStatus(
            Long recordId,
            RecordStatus status,
            Long userId) {

        Record record =
                recordRepository.findById(recordId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Record not found"
                                ));

        verifyRecordAccess(record, userId);
        verifyRecordOwner(record, userId);

        record.setStatus(status);

        Record updatedRecord =
                recordRepository.save(record);

        return mapToDTO(updatedRecord);
    }

    private RecordDTO mapToDTO(Record record) {

        RecordDTO dto = new RecordDTO();

        dto.setId(record.getId());
        dto.setTitle(record.getTitle());
        dto.setDescription(record.getDescription());
        dto.setType(record.getType());
        dto.setStatus(record.getStatus());
        dto.setProjectId(record.getProject().getId());
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

    private void verifyRecordOwner(
            Record record,
            Long userId) {

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

    private void verifyRecordAccess(Record record, Long userId) {
        projectAccessService.requireMember(record.getProject().getId(), userId);
    }

    public void verifyProjectAccess(Long projectId, Long userId) {
        projectAccessService.requireMember(projectId, userId);
    }

    private void validateImage(
            MultipartFile image) {

        if (image.isEmpty()) {
            throw new IllegalArgumentException(
                    "Image is empty"
            );
        }

        String contentType =
                image.getContentType();

        if (contentType == null ||
                !contentType.startsWith("image/")) {

            throw new IllegalArgumentException(
                    "Invalid file type"
            );
        }

        if (image.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "File too large"
            );
        }

        try {
            BufferedImage decodedImage = ImageIO.read(image.getInputStream());
            if (decodedImage == null || decodedImage.getWidth() <= 0 || decodedImage.getHeight() <= 0) {
                throw new IllegalArgumentException("Invalid image content");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read image content", e);
        }
    }
}
