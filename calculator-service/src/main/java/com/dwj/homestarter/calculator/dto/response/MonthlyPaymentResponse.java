package com.dwj.homestarter.calculator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대출 월 상환액 계산 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyPaymentResponse {

    /**
     * 월 상환액 (원)
     */
    private Long monthlyPayment;
}
