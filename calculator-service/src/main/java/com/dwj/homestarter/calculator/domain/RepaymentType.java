package com.dwj.homestarter.calculator.domain;

/**
 * 대출 상환 유형 열거형
 * asset-service의 RepaymentType과 동일한 enum
 */
public enum RepaymentType {
    /**
     * 원금균등 (Equal Principal)
     */
    EP,

    /**
     * 원리금균등 (Equal Principal and Interest)
     */
    EPI,

    /**
     * 만기일시 (Maturity date and time)
     */
    MDT,

    /**
     * 체증식 (Gradual Growth)
     */
    GG
}
