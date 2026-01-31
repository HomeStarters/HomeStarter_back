package com.dwj.homestarter.asset.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 월소득 항목 도메인 모델
 * 급여, 부업 등 월소득의 상세 항목을 표현
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeItem {

    /**
     * 월소득 항목 ID
     */
    private String id;

    /**
     * 소득명 (예: 회사 급여)
     */
    private String name;

    /**
     * 월 금액 (원)
     */
    private Long amount;
}
