package com.fieldclinic.javabackend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // 根据用户和日期查询当天的任务
    List<Task> findByUserIdAndTaskDateOrderByTaskTimeAsc(String userId, LocalDate taskDate);

    // 根据用户查询所有任务
    List<Task> findByUserIdOrderByTaskDateDesc(String userId);

    // 根据用户和 ID 查询，确保修改和删除时校验归属
    java.util.Optional<Task> findByIdAndUserId(Long id, String userId);
}