package com.fieldclinic.javabackend;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetectRecordRepository extends JpaRepository<DetectRecord, Long> {
    List<DetectRecord> findByUserIdAndTypeOrderByCreatedAtDesc(String userId, String type);
    List<DetectRecord> findByUserIdOrderByCreatedAtDesc(String userId);
    Page<DetectRecord> findByUserId(String userId, Pageable pageable);
    Page<DetectRecord> findByUserIdAndType(String userId, String type, Pageable pageable);
}