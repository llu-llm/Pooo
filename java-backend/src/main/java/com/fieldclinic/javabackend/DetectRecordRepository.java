package com.fieldclinic.javabackend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetectRecordRepository extends JpaRepository<DetectRecord, Long> {
    List<DetectRecord> findByUserIdAndTypeOrderByCreatedAtDesc(String userId, String type);
    List<DetectRecord> findByUserIdOrderByCreatedAtDesc(String userId);
}