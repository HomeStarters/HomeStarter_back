package com.dwj.homestarter.calculator.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 자산 정보 외부 DTO
 * Asset Service로부터 받아오는 데이터
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetDto {

    /**
     * 사용자 ID
     */
    private String userId;

    /**
     * 총 자산 (원)
     */
    private Long totalAssets;

    /**
     * 총 대출 (원)
     */
    private Long totalLoans;

    /**
     * 월 소득 (원)
     */
    private Long monthlyIncome;

    /**
     * 월 지출 (원)
     */
    private Long monthlyExpenses;

    /**
     * 개별 대출 항목 목록
     */
    private List<LoanItemInfo> loanItems;

    /**
     * 대출 항목 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoanItemInfo {
        private Long amount;
        private Double interestRate;
        private LocalDate expirationDate;
        private boolean isExcludingCalculation;
    }
}
