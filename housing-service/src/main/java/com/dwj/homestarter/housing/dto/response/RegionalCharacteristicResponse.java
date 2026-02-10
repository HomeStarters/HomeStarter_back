package com.dwj.homestarter.housing.dto.response;

import com.dwj.homestarter.housing.domain.enums.RegionCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 지역 특성 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "지역 특성 정보")
public class RegionalCharacteristicResponse {

    @Schema(description = "지역 코드", example = "OHA")
    private RegionCode regionCode;

    @Schema(description = "지역 설명", example = "투기과열지구")
    private String regionDescription;

    @Schema(description = "LTV (담보인정비율)", example = "0.40")
    private BigDecimal ltv;

    @Schema(description = "DTI (총부채상환비율)", example = "0.40")
    private BigDecimal dti;
}
