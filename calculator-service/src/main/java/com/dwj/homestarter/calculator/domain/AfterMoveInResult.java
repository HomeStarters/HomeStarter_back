package com.dwj.homestarter.calculator.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 입주 후 재무상태 결과
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfterMoveInResult {

    /**
     * 입주 후 자산 (원)
     */
    private Long assets;

    /**
     * 월 지출 (원)
     */
    private Long monthlyExpenses;

    /**
     * 월 소득 (원)
     */
    private Long monthlyIncome;

    /**
     * 여유 자금 (원)
     */
    private Long availableFunds;
}
