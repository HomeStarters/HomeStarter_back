package com.dwj.homestarter.calculator.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 지역 특성 정보 외부 DTO
 * Housing Service로부터 받아오는 지역별 LTV/DTI 규제 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionalCharacteristicDto {

    /**
     * 지역 코드 (LTPZ: 토지거래허가구역, OHA: 투기과열지구, AA: 조정대상지역, G: 일반지역)
     */
    private String regionCode;

    /**
     * 지역 설명
     */
    private String regionDescription;

    /**
     * LTV 한도 (0.0 ~ 1.0, 예: 0.4 = 40%)
     */
    private Double ltv;

    /**
     * DTI 한도 (0.0 ~ 1.0, 예: 0.4 = 40%)
     */
    private Double dti;
}
