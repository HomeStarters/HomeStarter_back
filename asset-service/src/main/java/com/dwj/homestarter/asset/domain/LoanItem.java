package com.dwj.homestarter.asset.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 대출 항목 도메인 모델
 * 주택담보대출, 신용대출 등 대출의 상세 항목을 표현
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanItem {

    /**
     * 대출 항목 ID
     */
    private String id;

    /**
     * 대출명 (예: 주택담보대출)
     */
    private String name;

    /**
     * 대출 잔액 (원)
     */
    private Long amount;

    /**
     * 금리 (연 %)
     */
    private Double interestRate;

    /**
     * 대출 유형 (주택담보, 전세, 신용, 기타)
     */
    private LoanType loanType;

    /**
     * 상환 유형 (원금균등, 원리금균등, 만기일시, 체증식)
     */
    private RepaymentType repaymentType;

    /**
     * 만기일
     */
    private LocalDate expirationDate;

    /**
     * 계산제외 여부
     */
    private Boolean isExcludingCalculation;

    /**
     * 대출실행 금액 (원)
     */
    private Long executedAmount;

    /**
     * 상환기간 (개월)
     */
    private Integer repaymentPeriod;

    /**
     * 거치기간 (개월)
     */
    private Integer gracePeriod;
}
