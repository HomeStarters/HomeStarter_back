package com.dwj.homestarter.asset.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 자산 요약 정보 도메인 모델
 * 자산의 총액 정보를 요약하여 표현
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetSummary {

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

    /**
     * Asset 도메인 객체로부터 AssetSummary 생성
     *
     * @param asset Asset 도메인 객체
     * @return AssetSummary
     */
    public static AssetSummary fromAsset(Asset asset) {
        return AssetSummary.builder()
                .totalAssets(asset.getTotalAssets())
                .totalLoans(asset.getTotalLoans())
                .totalMonthlyIncome(asset.getTotalMonthlyIncome())
                .totalMonthlyExpense(asset.getTotalMonthlyExpense())
                .netAssets(asset.getNetAssets())
                .monthlyAvailableFunds(asset.getMonthlyAvailableFunds())
                .build();
    }
}
