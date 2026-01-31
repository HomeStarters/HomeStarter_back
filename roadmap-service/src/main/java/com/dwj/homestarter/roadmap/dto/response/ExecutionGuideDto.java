package com.dwj.homestarter.roadmap.dto.response;

import lombok.*;

import java.util.List;

/**
 * 실행 가이드 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionGuideDto {

    /**
     * 월별 저축 플랜
     */
    private List<MonthlySavingsPlanDto> monthlySavingsPlan;

    /**
     * 주의사항
     */
    private List<String> warnings;

    /**
     * 팁
     */
    private List<String> tips;
}
