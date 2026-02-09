package com.dwj.homestarter.housing.dto.response;

import com.dwj.homestarter.housing.domain.enums.HousingType;
import com.dwj.homestarter.housing.domain.enums.RegionCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "주택 목록 항목")
public class HousingListItem {
    @Schema(description = "주택 ID")
    private Long id;

    @Schema(description = "주택명")
    private String housingName;

    @Schema(description = "주택 유형")
    private HousingType housingType;

    @Schema(description = "가격 (원)")
    private BigDecimal price;

    @Schema(description = "전체 주소")
    private String fullAddress;

    @Schema(description = "지역 코드", example = "OHA")
    private RegionCode regionCode;

    @Schema(description = "지역 설명", example = "투기과열지구")
    private String regionDescription;

    @Schema(description = "최종목표 주택 여부")
    private Boolean isGoal;

    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;
}
