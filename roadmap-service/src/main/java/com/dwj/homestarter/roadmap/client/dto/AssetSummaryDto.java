package com.dwj.homestarter.roadmap.client.dto;

import lombok.*;

/**
 * Asset Service로부터 받는 자산 요약 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetSummaryDto {

    /**
     * 본인 총 자산
     */
    private Long myTotalAssets;

    /**
     * 배우자 총 자산
     */
    private Long spouseTotalAssets;

    /**
     * 본인 월 소득
     */
    private Long myMonthlyIncome;

    /**
     * 배우자 월 소득
     */
    private Long spouseMonthlyIncome;

    /**
     * 월 총 지출
     */
    private Long monthlyExpense;
}
