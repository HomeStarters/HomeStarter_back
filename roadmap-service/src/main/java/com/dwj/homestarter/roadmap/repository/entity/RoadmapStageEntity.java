package com.dwj.homestarter.roadmap.repository.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 로드맵 단계 엔티티
 * 로드맵의 각 단계별 주택 계획 및 재무 목표를 저장
 */
@Entity
@Table(name = "roadmap_stages",
       indexes = {
           @Index(name = "idx_roadmap_stages_roadmap_id", columnList = "roadmap_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_roadmap_stages_roadmap_stage", columnNames = {"roadmap_id", "stage_number"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapStageEntity {

    /**
     * 단계 고유 ID (UUID)
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
     * 단계 번호 (1부터 시작)
     */
    @Column(name = "stage_number", nullable = false)
    private Integer stageNumber;

    /**
     * 단계명 (예: "신혼기 전세", "육아기 매매")
     */
    @Column(name = "stage_name", nullable = false, length = 100)
    private String stageName;

    /**
     * 입주 시기 (yyyy-MM 형식)
     */
    @Column(name = "move_in_date", nullable = false, length = 7)
    private String moveInDate;

    /**
     * 거주 기간 (개월 수)
     */
    @Column(nullable = false)
    private Integer duration;

    /**
     * 예상 가격 (원)
     */
    @Column(name = "estimated_price", nullable = false)
    private Long estimatedPrice;

    /**
     * 위치
     */
    @Column(nullable = false, length = 200)
    private String location;

    /**
     * 주택 타입 (전세, 매매, 월세 등)
     */
    @Column(nullable = false, length = 50)
    private String type;

    /**
     * 특징 (JSON 배열)
     */
    @Column(columnDefinition = "TEXT")
    private String features;

    /**
     * 목표 저축액 (원)
     */
    @Column(name = "target_savings", nullable = false)
    private Long targetSavings;

    /**
     * 월 저축액 (원)
     */
    @Column(name = "monthly_savings", nullable = false)
    private Long monthlySavings;

    /**
     * 대출 금액 (원)
     */
    @Column(name = "loan_amount")
    private Long loanAmount;

    /**
     * 대출 상품명
     */
    @Column(name = "loan_product", length = 100)
    private String loanProduct;

    /**
     * 실행 전략
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String strategy;

    /**
     * 팁 (JSON 배열)
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
