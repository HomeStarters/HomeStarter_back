package com.dwj.homestarter.roadmap.dto.response;

import lombok.*;

import java.util.List;

/**
 * 로드맵 단계 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapStageDto {

    /**
     * 단계 번호
     */
    private Integer stageNumber;

    /**
     * 단계명
     */
    private String stageName;

    /**
     * 입주 시기 (yyyy-MM)
     */
    private String moveInDate;

    /**
     * 거주 기간 (개월)
     */
    private Integer duration;

    /**
     * 주택 특징
     */
    private HousingCharacteristicsDto housingCharacteristics;

    /**
     * 재무 목표
     */
    private FinancialGoalsDto financialGoals;

    /**
     * 실행 전략
     */
    private String strategy;

    /**
     * 팁 목록
     */
    private List<String> tips;
}
