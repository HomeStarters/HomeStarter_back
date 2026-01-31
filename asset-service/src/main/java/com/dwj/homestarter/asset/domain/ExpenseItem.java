package com.dwj.homestarter.asset.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 월지출 항목 도메인 모델
 * 생활비, 교육비 등 월지출의 상세 항목을 표현
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseItem {

    /**
     * 월지출 항목 ID
     */
    private String id;

    /**
     * 지출명 (예: 생활비)
     */
    private String name;

    /**
     * 월 금액 (원)
     */
    private Long amount;
}
