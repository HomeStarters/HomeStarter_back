package com.dwj.homestarter.asset.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 자산 항목 도메인 모델
 * 예금, 적금, 주식 등 자산의 상세 항목을 표현
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetItem {

    /**
     * 자산 항목 ID
     */
    private String id;

    /**
     * 자산명 (예: 국민은행 예금)
     */
    private String name;

    /**
     * 금액 (원)
     */
    private Long amount;
}
