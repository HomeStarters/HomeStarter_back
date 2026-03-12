package com.dwj.homestarter.calculator.dto.request;

import com.dwj.homestarter.calculator.domain.LoanType;
import com.dwj.homestarter.calculator.domain.RepaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대출 월 상환액 계산 요청 DTO
 * 단일 대출의 월 상환액 계산에 필요한 파라미터
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyPaymentRequest {

    /**
     * 대출 유형 (MORTGAGE, JEONSE, CREDIT, OTHER) - 기본값: OTHER
     */
    private LoanType loanType;

    /**
     * 상환 유형 (EP, EPI, MDT, GG) - 기본값: EPI
     */
    private RepaymentType repaymentType;

    /**
     * 대출 원금 (원)
     */
    @NotNull(message = "대출 금액은 필수입니다")
    @Positive(message = "대출 금액은 양수여야 합니다")
    private Long loanAmount;

    /**
     * 연 이자율 (%)
     */
    @NotNull(message = "연 이자율은 필수입니다")
    @Positive(message = "연 이자율은 양수여야 합니다")
    private Double annualInterestRate;

    /**
     * 상환기간 (개월)
     */
    @NotNull(message = "상환기간은 필수입니다")
    @Positive(message = "상환기간은 양수여야 합니다")
    private Integer repaymentPeriod;

    /**
     * 거치기간 (개월) - 기본값: 0
     */
    @PositiveOrZero(message = "거치기간은 0 이상이어야 합니다")
    private Integer gracePeriod;
}
