package com.dwj.homestarter.asset.domain;

/**
 * 대출 유형 열거형
 * DSR 계산 시 대출 유형에 따라 계산 방식이 달라짐
 */
public enum LoanType {
    /**
     * 주택담보대출 (Mortgage)
     */
    MORTGAGE,

    /**
     * 전세대출 (Jeonse Loan)
     */
    JEONSE,

    /**
     * 신용대출 (Credit Loan)
     */
    CREDIT,

    /**
     * 기타대출 (Other Loan)
     */
    OTHER
}
