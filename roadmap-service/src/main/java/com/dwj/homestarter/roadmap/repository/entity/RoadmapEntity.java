package com.dwj.homestarter.roadmap.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 로드맵 엔티티
 * AI가 생성한 장기주거 로드맵 정보를 저장
 */
@Entity
@Table(name = "roadmaps",
       indexes = {
           @Index(name = "idx_roadmaps_user_id", columnList = "user_id"),
           @Index(name = "idx_roadmaps_task_id", columnList = "task_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_roadmaps_user_version", columnNames = {"user_id", "version"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapEntity {

    /**
     * 로드맵 고유 ID (UUID)
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
     * 로드맵 버전 (1부터 시작)
     */
    @Column(nullable = false)
    private Integer version;

    /**
     * 로드맵 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoadmapStatus status;

    /**
     * 비동기 작업 ID (roadmap_tasks 참조)
     */
    @Column(name = "task_id", nullable = false, length = 36)
    private String taskId;

    /**
     * 최종목표 주택 ID (외부 참조)
     */
    @Column(name = "final_housing_id", nullable = false, length = 36)
    private String finalHousingId;

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

    @PrePersist
    private void generateId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }
}
