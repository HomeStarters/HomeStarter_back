package com.dwj.homestarter.calculator.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 대출상품 정보 외부 DTO
 * Loan Service로부터 받아오는 데이터
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProductDto {

    /**
     * 대출상품 ID
     */
    private String loanProductId;

    /**
     * 대출상품 이름
     */
    private String name;

    /**
     * LTV 한도 (%)
     */
    private Double ltvLimit;

    /**
     * DTI 한도 (%)
     */
    private Double dtiLimit;

    /**
     * DSR 한도 (%)
     */
    private Double dsrLimit;

    /**
     * 금리 (%)
     */
    private Double interestRate;

    /**
     * 최대 대출 금액 (원)
     */
    private Long maxAmount;
}
