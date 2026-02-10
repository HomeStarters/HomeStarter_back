package com.dwj.homestarter.calculator.dto.external.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Loan Service 실제 응답 구조
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanProductResponse {
    private boolean success;
    private LoanProductData data;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanProductData {
        private Long id;
        private String name;
        private Long loanLimit;
//        private Double ltvLimit;
//        private Double dtiLimit;
        private Double dsrLimit;
        private Boolean isApplyLtv;
        private Boolean isApplyDti;
        private Boolean isApplyDsr;
        private Double interestRate;
        private String targetHousing;
        private String incomeRequirement;
        private String applicantRequirement;
        private String remarks;
        private Boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
