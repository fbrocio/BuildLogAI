package com.example.demo.service;

import com.example.demo.dto.RecordDTO;
import com.example.demo.model.RecordStatus;
import com.example.demo.model.RecordType;
import com.example.demo.model.Project;
import com.example.demo.model.User;
import com.example.demo.model.Record;
import com.example.demo.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecordImageRepository recordImageRepository;

    @Mock
    private UserInputRepository userInputRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectAccessService projectAccessService;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private RecordService recordService;

    @Test
    void createRecord_shouldCreateRecordWhenUserExists() {

        // Arrange
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        Project project = new Project();
        project.setId(10L);

        RecordDTO dto = new RecordDTO();
        dto.setTitle("Prueba");
        dto.setDescription("Descripción de prueba");
        dto.setType(RecordType.AVANCE);
        dto.setStatus(RecordStatus.ABIERTA);
        dto.setProjectId(10L);

        Record savedRecord = new Record();
        savedRecord.setId(100L);
        savedRecord.setTitle(dto.getTitle());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        when(recordRepository.save(any(Record.class)))
                .thenReturn(savedRecord);

        // Act
        Record result = recordService.createRecord(dto, userId);

        // Assert
        assertEquals(100L, result.getId());
        assertEquals("Prueba", result.getTitle());

        verify(userRepository).findById(userId);
        verify(projectAccessService).requireMember(10L, userId);
        verify(recordRepository).save(any(Record.class));
    }

    @Test
    void createRecord_shouldNotPersistWhenUserIsNotProjectMember() {

        Long userId = 1L;
        RecordDTO dto = new RecordDTO();
        dto.setProjectId(10L);

        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(projectAccessService).requireMember(10L, userId);

        assertThrows(
                ResponseStatusException.class,
                () -> recordService.createRecord(dto, userId)
        );

        verify(recordRepository, never()).save(any(Record.class));
    }
    @Test
    void createRecord_shouldThrowExceptionWhenUserDoesNotExist() {

        // Arrange
        Long userId = 99L;

        RecordDTO dto = new RecordDTO();
        dto.setTitle("Prueba");
        dto.setDescription("Descripción de prueba");
        dto.setType(RecordType.AVANCE);
        dto.setStatus(RecordStatus.ABIERTA);
        dto.setProjectId(10L);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                NoSuchElementException.class,
                () -> recordService.createRecord(dto, userId)
        );

        verify(userRepository).findById(userId);
        verify(recordRepository, never()).save(any(Record.class));
    }
}
