package com.dwj.homestarter.housing.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 지역 코드
 * 특수 지역별 특성 기준 코드
 */
@Getter
@RequiredArgsConstructor
public enum RegionCode {
    /**
     * 토지거래허가구역
     */
    LTPZ("토지거래허가구역"),

    /**
     * 투기과열지구
     */
    OHA("투기과열지구"),

    /**
     * 조정대상지역
     */
    AA("조정대상지역"),

    /**
     * 일반지역
     */
    G("일반지역");

    private final String description;
}
