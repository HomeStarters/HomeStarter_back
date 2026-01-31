package com.dwj.homestarter.housing.domain.model;

import lombok.*;

import java.math.BigDecimal;

/**
 * 단지 정보 값 객체
 * JSONB 컬럼으로 저장될 복합 정보
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplexInfo {

    /**
     * 단지명
     */
    private String complexName;

    /**
     * 총 세대수
     */
    private Integer totalHouseholds;

    /**
     * 총 동수
     */
    private Integer totalDong;

    /**
     * 총 층수
     */
    private Integer totalFloors;

    /**
     * 주차 대수
     */
    private Integer parkingCount;

    /**
     * 입주년월 (YYYY-MM)
     */
    private String moveInDate;

    /**
     * 건설사
     */
    private String constructionCompany;

    /**
     * 공급면적 (㎡)
     */
    private BigDecimal houseArea;

    /**
     * 전용면적 (㎡)
     */
    private BigDecimal exclusiveArea;

    /**
     * 층수
     */
    private Integer floor;

    /**
     * 향
     */
    private String direction;
}
