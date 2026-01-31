package com.dwj.homestarter.asset.repository.entity;

import com.dwj.homestarter.asset.domain.Asset;
import com.dwj.homestarter.asset.domain.OwnerType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 자산정보 JPA 엔티티
 * 본인/배우자별 자산 총액 정보를 저장
 */
@Entity
@Table(name = "assets", schema = "asset_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetEntity {

    /**
     * 자산정보 ID (UUID)
     */
    @Id
    @Column(name = "id", length = 50)
    private String id;

    /**
     * 사용자 ID
     */
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    /**
     * 소유자 유형 (SELF, SPOUSE)
     */
    @Column(name = "owner_type", nullable = false, length = 10)
    private String ownerType;

    /**
     * 총 자산액 (원)
     */
    @Column(name = "total_assets", nullable = false)
    @Builder.Default
    private Long totalAssets = 0L;

    /**
     * 총 대출액 (원)
     */
    @Column(name = "total_loans", nullable = false)
    @Builder.Default
    private Long totalLoans = 0L;

    /**
     * 총 월소득 (원)
     */
    @Column(name = "total_monthly_income", nullable = false)
    @Builder.Default
    private Long totalMonthlyIncome = 0L;

    /**
     * 총 월지출 (원)
     */
    @Column(name = "total_monthly_expense", nullable = false)
    @Builder.Default
    private Long totalMonthlyExpense = 0L;

    /**
     * 순자산 (총자산 - 총대출)
     */
    @Column(name = "net_assets", nullable = false)
    @Builder.Default
    private Long netAssets = 0L;

    /**
     * 월 가용자금 (월소득 - 월지출)
     */
    @Column(name = "monthly_available_funds", nullable = false)
    @Builder.Default
    private Long monthlyAvailableFunds = 0L;

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
     * @return Asset 도메인 객체
     */
    public Asset toDomain() {
        return Asset.builder()
                .id(this.id)
                .userId(this.userId)
                .ownerType(OwnerType.valueOf(this.ownerType))
                .totalAssets(this.totalAssets)
                .totalLoans(this.totalLoans)
                .totalMonthlyIncome(this.totalMonthlyIncome)
                .totalMonthlyExpense(this.totalMonthlyExpense)
                .netAssets(this.netAssets)
                .monthlyAvailableFunds(this.monthlyAvailableFunds)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    /**
     * 도메인 객체로부터 엔티티 생성
     *
     * @param asset Asset 도메인 객체
     * @return AssetEntity
     */
    public static AssetEntity fromDomain(Asset asset) {
        return AssetEntity.builder()
                .id(asset.getId())
                .userId(asset.getUserId())
                .ownerType(asset.getOwnerType().name())
                .totalAssets(asset.getTotalAssets())
                .totalLoans(asset.getTotalLoans())
                .totalMonthlyIncome(asset.getTotalMonthlyIncome())
                .totalMonthlyExpense(asset.getTotalMonthlyExpense())
                .netAssets(asset.getNetAssets())
                .monthlyAvailableFunds(asset.getMonthlyAvailableFunds())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .build();
    }
}
