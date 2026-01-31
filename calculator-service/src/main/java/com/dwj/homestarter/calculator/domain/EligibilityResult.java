package com.dwj.homestarter.calculator.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 적격성 판단 결과
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityResult {

    /**
     * 대출 적격 여부
     */
    private Boolean isEligible;

    /**
     * 미충족 사유 목록
     */
    private List<String> reasons;
}
