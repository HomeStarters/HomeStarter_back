package com.dwj.homestarter.asset.domain;

/**
 * 대출 상환 유형 열거형
 * 보유중인 채무액이 어떤 상환 유형인지 구분
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
