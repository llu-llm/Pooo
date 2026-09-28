package com.fieldclinic.javabackend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface FarmRecordRepository extends JpaRepository<FarmRecord, Long> {

    List<FarmRecord> findByUserIdAndRecordDateOrderByRecordTimeAsc(String userId, LocalDate recordDate);

    List<FarmRecord> findByUserIdOrderByRecordDateDescRecordTimeDesc(String userId);
}