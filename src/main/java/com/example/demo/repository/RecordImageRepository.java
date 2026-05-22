package com.example.demo.repository;

import com.example.demo.model.RecordImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordImageRepository extends JpaRepository<RecordImage, Long> {

    List<RecordImage> findByRecordId(Long recordId);
}
