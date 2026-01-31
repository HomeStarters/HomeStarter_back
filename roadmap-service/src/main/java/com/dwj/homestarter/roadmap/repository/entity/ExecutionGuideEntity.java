package com.dwj.homestarter.roadmap.repository.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 실행 가이드 엔티티
 * 로드맵 실행을 위한 가이드 정보를 저장
 */
@Entity
@Table(name = "execution_guides",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_execution_guides_roadmap", columnNames = {"roadmap_id"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionGuideEntity {

    /**
     * 가이드 고유 ID (UUID)
     */
    @Id
    @Column(length = 36)
    private String id;

    /**
     * 로드맵 ID (roadmaps 참조)
     */
    @Column(name = "roadmap_id", nullable = false, length = 36)
    private String roadmapId;

    /**
     * 월별 저축 플랜 (JSON 배열)
     * 예: [{"period": "2025-01 ~ 2027-12", "amount": 2000000, "purpose": "전세 보증금 마련"}]
     */
    @Column(name = "monthly_savings_plan", nullable = false, columnDefinition = "TEXT")
    private String monthlySavingsPlan;

    /**
     * 주의사항 (JSON 배열)
     * 예: ["금리 상승 시 월 저축액 재검토", "생애주기 이벤트 발생 시 재설계 권장"]
     */
    @Column(columnDefinition = "TEXT")
    private String warnings;

    /**
     * 팁 (JSON 배열)
     * 예: ["적금 만기 시 즉시 재예치", "보너스는 100% 저축"]
     */
    @Column(columnDefinition = "TEXT")
    private String tips;

    @PrePersist
    private void generateId() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }
}
