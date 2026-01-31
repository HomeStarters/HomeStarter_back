package com.dwj.homestarter.asset.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 가구 전체 자산 요약 DTO
 * 본인 + 배우자 자산을 합산한 정보
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CombinedAssetSummaryDto {

    /**
     * 총 자산액 (원)
     */
    private Long totalAssets;

    /**
     * 총 대출액 (원)
     */
    private Long totalLoans;

    /**
     * 총 월소득 (원)
     */
    private Long totalMonthlyIncome;

    /**
     * 총 월지출 (원)
     */
    private Long totalMonthlyExpense;

    /**
     * 순자산 (원)
     */
    private Long netAssets;

    /**
     * 월 가용자금 (원)
     */
    private Long monthlyAvailableFunds;
}
