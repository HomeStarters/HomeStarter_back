package com.dwj.homestarter.calculator.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 계산 결과 엔티티
 * 입주 후 지출 계산 결과를 저장하는 핵심 테이블
 */
@Entity
@Table(name = "calculation_results", schema = "calculator_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculationResultEntity {

    /**
     * 계산 결과 ID (UUID)
     */
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    /**
     * 사용자 ID
     */
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    /**
     * 주택 ID
     */
    @Column(name = "housing_id", length = 36, nullable = false)
    private String housingId;

    /**
     * 주택 이름
     */
    @Column(name = "housing_name", length = 200, nullable = false)
    private String housingName;

    /**
     * 입주 예정일
     */
    @Column(name = "move_in_date", nullable = false)
    private LocalDate moveInDate;

    /**
     * 대출상품 ID
     */
    @Column(name = "loan_product_id", length = 36, nullable = false)
    private String loanProductId;

    /**
     * 대출상품 이름
     */
    @Column(name = "loan_product_name", length = 200, nullable = false)
    private String loanProductName;

    /**
     * 대출 금액 (원)
     */
    @Column(name = "loan_amount", nullable = false)
    private Long loanAmount;

    /**
     * 대출 기간 (개월)
     */
    @Column(name = "loan_term", nullable = false)
    private Integer loanTerm;

    /**
     * 현재 순자산 (원)
     */
    @Column(name = "current_assets", nullable = false)
    private Long currentAssets;

    /**
     * 예상자산 (원)
     */
    @Column(name = "estimated_assets", nullable = false)
    private Long estimatedAssets;

    /**
     * 대출필요금액 (원)
     */
    @Column(name = "loan_required", nullable = false)
    private Long loanRequired;

    /**
     * 계산된 LTV (%)
     */
    @Column(name = "ltv", nullable = false)
    private Double ltv;

    /**
     * 계산된 DTI (%)
     */
    @Column(name = "dti", nullable = false)
    private Double dti;

    /**
     * 계산된 DSR (%)
     */
    @Column(name = "dsr", nullable = false)
    private Double dsr;

    /**
     * LTV 한도 (%)
     */
    @Column(name = "ltv_limit", nullable = false)
    private Double ltvLimit;

    /**
     * DTI 한도 (%)
     */
    @Column(name = "dti_limit", nullable = false)
    private Double dtiLimit;

    /**
     * DSR 한도 (%)
     */
    @Column(name = "dsr_limit", nullable = false)
    private Double dsrLimit;

    /**
     * 대출 적격 여부
     */
    @Column(name = "is_eligible", nullable = false)
    private Boolean isEligible;

    /**
     * 미충족 사유 (JSON)
     */
    @Column(name = "ineligibility_reasons", columnDefinition = "TEXT")
    private String ineligibilityReasons;

    /**
     * 월 상환액 (원)
     */
    @Column(name = "monthly_payment", nullable = false)
    private Long monthlyPayment;

    /**
     * 입주 후 자산 (원)
     */
    @Column(name = "after_move_in_assets", nullable = false)
    private Long afterMoveInAssets;

    /**
     * 입주 후 월지출 (원)
     */
    @Column(name = "after_move_in_monthly_expenses", nullable = false)
    private Long afterMoveInMonthlyExpenses;

    /**
     * 월소득 (원)
     */
    @Column(name = "after_move_in_monthly_income", nullable = false)
    private Long afterMoveInMonthlyIncome;

    /**
     * 여유자금 (원)
     */
    @Column(name = "after_move_in_available_funds", nullable = false)
    private Long afterMoveInAvailableFunds;

    /**
     * 상태 (ELIGIBLE, INELIGIBLE)
     */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    /**
     * 계산 일시
     */
    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    /**
     * 생성 일시
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
