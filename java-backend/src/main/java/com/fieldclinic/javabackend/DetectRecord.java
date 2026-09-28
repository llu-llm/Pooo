package com.fieldclinic.javabackend;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "detect_record", indexes = {
        @Index(name = "idx_detect_user_created", columnList = "user_id,created_at"),
        @Index(name = "idx_detect_user_type_created", columnList = "user_id,type,created_at")
})
public class DetectRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "type", nullable = false, length = 20)
    private String type;  // disease / fruit

    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}