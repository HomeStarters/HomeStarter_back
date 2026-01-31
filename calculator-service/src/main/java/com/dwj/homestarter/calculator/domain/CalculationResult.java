package com.dwj.homestarter.calculator.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 계산 결과 도메인 모델
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculationResult {

    /**
     * 예상자산 (원)
     */
    private Long estimatedAssets;

    /**
     * 대출필요금액 (원)
     */
    private Long loanRequired;

    /**
     * 계산된 LTV (%)
     */
    private Double ltv;

    /**
     * 계산된 DTI (%)
     */
    private Double dti;

    /**
     * 계산된 DSR (%)
     */
    private Double dsr;

    /**
     * 대출 적격 여부
     */
    private Boolean isEligible;

    /**
     * 미충족 사유 목록
     */
    private List<String> ineligibilityReasons;

    /**
     * 월 상환액 (원)
     */
    private Long monthlyPayment;

    /**
     * 입주 후 자산 (원)
     */
    private Long afterMoveInAssets;

    /**
     * 입주 후 월 지출 (원)
     */
    private Long afterMoveInMonthlyExpenses;

    /**
     * 입주 후 월 소득 (원)
     */
    private Long afterMoveInMonthlyIncome;

    /**
     * 입주 후 여유 자금 (원)
     */
    private Long afterMoveInAvailableFunds;
}
