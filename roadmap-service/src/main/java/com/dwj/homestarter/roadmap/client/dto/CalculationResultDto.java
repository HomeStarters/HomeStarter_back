package com.dwj.homestarter.roadmap.client.dto;

import lombok.*;

/**
 * Calculator Service로부터 받는 계산 결과 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculationResultDto {

    /**
     * 월 가용 자금
     */
    private Long monthlyAvailableFunds;

    /**
     * 추천 대출 금액
     */
    private Long recommendedLoanAmount;

    /**
     * 추천 대출 상품
     */
    private String recommendedLoanProduct;

    /**
     * 대출 충족 여부
     */
    private Boolean loanEligible;
}
