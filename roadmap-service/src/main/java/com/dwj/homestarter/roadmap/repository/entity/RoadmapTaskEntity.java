package com.dwj.homestarter.roadmap.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 비동기 작업 추적 엔티티
 * AI 로드맵 생성 비동기 작업의 상태를 추적
 */
@Entity
@Table(name = "roadmap_tasks",
       indexes = {
           @Index(name = "idx_roadmap_tasks_user_id", columnList = "user_id"),
           @Index(name = "idx_roadmap_tasks_status", columnList = "status")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapTaskEntity {

    /**
     * 작업 고유 ID (UUID)
     */
    @Id
    @Column(length = 36)
    private String id;

    /**
     * 사용자 ID (외부 참조)
     */
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    /**
     * 작업 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    /**
     * 진행률 (0~100)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer progress = 0;

    /**
     * 진행 상황 메시지
     */
    @Column(length = 200)
    private String message;

    /**
     * 완료된 로드맵 ID (roadmaps 참조)
     */
    @Column(name = "roadmap_id", length = 36)
    private String roadmapId;

    /**
     * 에러 메시지 (실패 시)
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 생성일시
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 완료일시
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    private void generateId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    /**
     * 작업 완료 처리
     */
    public void markAsCompleted(String roadmapId) {
        this.status = TaskStatus.COMPLETED;
        this.progress = 100;
        this.roadmapId = roadmapId;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 작업 실패 처리
     */
    public void markAsFailed(String errorMessage) {
        this.status = TaskStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
}
