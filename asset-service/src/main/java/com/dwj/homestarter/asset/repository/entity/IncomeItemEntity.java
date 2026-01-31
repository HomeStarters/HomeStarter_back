package com.dwj.homestarter.asset.repository.entity;

import com.dwj.homestarter.asset.domain.IncomeItem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 월소득 항목 JPA 엔티티
 * 급여, 부업 등 월소득 상세 항목 저장
 */
@Entity
@Table(name = "income_items", schema = "asset_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeItemEntity {

    /**
     * 월소득 항목 ID (UUID)
     */
    @Id
    @Column(name = "id", length = 50)
    private String id;

    /**
     * 자산정보 ID (FK)
     */
    @Column(name = "asset_id", nullable = false, length = 50)
    private String assetId;

    /**
     * 소득명 (예: 회사 급여)
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 월 금액 (원)
     */
    @Column(name = "amount", nullable = false)
    private Long amount;

    /**
     * 생성 시간
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 도메인 객체로 변환
     *
     * @return IncomeItem 도메인 객체
     */
    public IncomeItem toDomain() {
        return IncomeItem.builder()
                .id(this.id)
                .name(this.name)
                .amount(this.amount)
                .build();
    }

    /**
     * 도메인 객체로부터 엔티티 생성
     *
     * @param assetId 자산정보 ID
     * @param item    IncomeItem 도메인 객체
     * @return IncomeItemEntity
     */
    public static IncomeItemEntity fromDomain(String assetId, IncomeItem item) {
        return IncomeItemEntity.builder()
                .id(item.getId())
                .assetId(assetId)
                .name(item.getName())
                .amount(item.getAmount())
                .build();
    }
}
