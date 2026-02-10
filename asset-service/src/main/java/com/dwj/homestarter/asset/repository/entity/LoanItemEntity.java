package com.dwj.homestarter.asset.repository.entity;

import com.dwj.homestarter.asset.domain.LoanItem;
import com.dwj.homestarter.asset.domain.RepaymentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
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
     * 금리 (연 %)
     */
    @Column(name = "interest_rate", nullable = false)
    private Double interestRate;

    /**
     * 상환 유형 (원금균등, 원리금균등, 만기일시, 체증식)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "repayment_type", nullable = false, length = 50)
    private RepaymentType repaymentType;

    /**
     * 만기일
     */
    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    /**
     * 계산제외 여부
     */
    @Column(name = "is_excluding_calculation", nullable = false)
    @Builder.Default
    private Boolean isExcludingCalculation = false;

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
                .interestRate(this.interestRate)
                .repaymentType(this.repaymentType)
                .expirationDate(this.expirationDate)
                .isExcludingCalculation(this.isExcludingCalculation)
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
                .interestRate(item.getInterestRate())
                .repaymentType(item.getRepaymentType())
                .expirationDate(item.getExpirationDate())
                .isExcludingCalculation(item.getIsExcludingCalculation())
                .build();
    }
}
