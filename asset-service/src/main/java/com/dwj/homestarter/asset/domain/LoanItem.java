package com.dwj.homestarter.asset.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
