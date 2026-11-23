package com.example.demo.repository;

import com.example.demo.model.Record;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {

    List<Record> findByProject_IdOrderByCreatedAtDesc(Long projectId);
    List<Record> findByProject_IdAndTitleContainingIgnoreCase(
            Long projectId,
            String title
    );
    List<Record> findByProject_IdAndCreatedAtBetween(
            Long projectId,
            LocalDateTime start,
            LocalDateTime end
    );
}
