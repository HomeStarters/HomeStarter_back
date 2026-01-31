package com.dwj.homestarter.roadmap.dto.response;

import lombok.*;

/**
 * 월별 저축 계획 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySavingsPlanDto {

    /**
     * 저축 기간 (예: "2025-06 ~ 2028-05")
     */
    private String period;

    /**
     * 월 저축액
     */
    private Long amount;

    /**
     * 저축 목적
     */
    private String purpose;
}
