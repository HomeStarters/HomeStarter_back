package com.dwj.homestarter.roadmap.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 생애주기 이벤트 엔티티
 * 사용자의 주요 생애주기 이벤트 정보를 저장
 */
@Entity
@Table(name = "lifecycle_events",
       indexes = {
           @Index(name = "idx_lifecycle_events_user_id", columnList = "user_id"),
           @Index(name = "idx_lifecycle_events_event_type", columnList = "event_type")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LifecycleEventEntity {

    /**
     * 이벤트 고유 ID (UUID)
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
     * 이벤트 이름
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 이벤트 유형
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private EventType eventType;

    /**
     * 이벤트 예정일 (yyyy-MM 형식)
     */
    @Column(name = "event_date", nullable = false, length = 7)
    private String eventDate;

    /**
     * 주택 선택 고려 기준
     */
    @Column(name = "housing_criteria", length = 200)
    private String housingCriteria;

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
