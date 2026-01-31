package com.dwj.homestarter.roadmap.dto.response;

import lombok.*;

/**
 * 재무 목표 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialGoalsDto {

    /**
     * 목표 저축액
     */
    private Long targetSavings;

    /**
     * 월 저축액
     */
    private Long monthlySavings;

    /**
     * 대출 금액
     */
    private Long loanAmount;

    /**
     * 추천 대출 상품
     */
    private String loanProduct;
}
