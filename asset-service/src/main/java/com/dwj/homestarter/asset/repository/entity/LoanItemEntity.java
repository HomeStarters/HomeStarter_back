package com.dwj.homestarter.asset.repository.entity;

import com.dwj.homestarter.asset.domain.LoanItem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 대출 항목 JPA 엔티티
 * 주택담보대출, 신용대출 등 대출 상세 항목 저장
 */
@Entity
@Table(name = "loan_items", schema = "asset_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanItemEntity {

    /**
     * 대출 항목 ID (UUID)
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
     * 대출명 (예: 주택담보대출)
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 대출 잔액 (원)
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
     * @return LoanItem 도메인 객체
     */
    public LoanItem toDomain() {
        return LoanItem.builder()
                .id(this.id)
                .name(this.name)
                .amount(this.amount)
                .build();
    }

    /**
     * 도메인 객체로부터 엔티티 생성
     *
     * @param assetId 자산정보 ID
     * @param item    LoanItem 도메인 객체
     * @return LoanItemEntity
     */
    public static LoanItemEntity fromDomain(String assetId, LoanItem item) {
        return LoanItemEntity.builder()
                .id(item.getId())
                .assetId(assetId)
                .name(item.getName())
                .amount(item.getAmount())
                .build();
    }
}
