package com.example.demo.repository;

import com.example.demo.model.Record;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {

    List<Record> findByProjectId(Long projectId);
    List<Record> findByTitleContainingIgnoreCase(String title);
    List<Record> findByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );
}
