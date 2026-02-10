package com.dwj.homestarter.calculator.dto.external.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Asset Service 실제 응답 구조
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetListResponse {
    private List<AssetResponse> assets;
    private CombinedAssetSummaryDto combinedSummary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetResponse {
        private String result;
        private String assetId;
        private String userId;
        private String ownerType;
        private List<LoanItemInfo> loans;
        private Long totalAssets;
        private Long totalLoans;
        private Long totalMonthlyIncome;
        private Long totalMonthlyExpense;
        private Long netAssets;
        private Long monthlyAvailableFunds;
    }

    /**
     * 대출 항목 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanItemInfo {
        private String id;
        private String name;
        private Long amount;
        private Double interestRate;
        private String repaymentType;
        private LocalDate expirationDate;
        private boolean isExcludingCalculation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CombinedAssetSummaryDto {
        private Long totalAssets;
        private Long totalLoans;
        private Long totalMonthlyIncome;
        private Long totalMonthlyExpense;
        private Long netAssets;
        private Long monthlyAvailableFunds;
    }
}
