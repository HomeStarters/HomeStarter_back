package com.dwj.homestarter.asset.repository.entity;

import com.dwj.homestarter.asset.domain.ExpenseItem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 월지출 항목 JPA 엔티티
 * 생활비, 교육비 등 월지출 상세 항목 저장
 */
@Entity
@Table(name = "expense_items", schema = "asset_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseItemEntity {

    /**
     * 월지출 항목 ID (UUID)
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
     * 지출명 (예: 생활비)
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
     * @return ExpenseItem 도메인 객체
     */
    public ExpenseItem toDomain() {
        return ExpenseItem.builder()
                .id(this.id)
                .name(this.name)
                .amount(this.amount)
                .build();
    }

    /**
     * 도메인 객체로부터 엔티티 생성
     *
     * @param assetId 자산정보 ID
     * @param item    ExpenseItem 도메인 객체
     * @return ExpenseItemEntity
     */
    public static ExpenseItemEntity fromDomain(String assetId, ExpenseItem item) {
        return ExpenseItemEntity.builder()
                .id(item.getId())
                .assetId(assetId)
                .name(item.getName())
                .amount(item.getAmount())
                .build();
    }
}
