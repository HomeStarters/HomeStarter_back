package com.dwj.homestarter.housing.dto.response;

import com.dwj.homestarter.housing.domain.enums.HousingType;
import com.dwj.homestarter.housing.domain.model.ComplexInfo;
import com.dwj.homestarter.housing.domain.model.LivingEnvironment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "주택 상세 응답")
public class HousingResponse {
    @Schema(description = "주택 ID")
    private Long id;

    @Schema(description = "주택명")
    private String housingName;

    @Schema(description = "주택 유형")
    private HousingType housingType;

    @Schema(description = "가격 (원)")
    private BigDecimal price;

    @Schema(description = "입주희망년월 (YYYY-MM)")
    private String moveInDate;

    @Schema(description = "준공일")
    private LocalDate completionDate;

    @Schema(description = "주소 정보")
    private String address;

//    @Schema(description = "주소 정보")
//    private AddressResponse address;

    @Schema(description = "지역 특성 정보")
    private RegionalCharacteristicResponse regionalCharacteristic;

    @Schema(description = "단지 정보")
    private ComplexInfo complexInfo;

    @Schema(description = "생활환경 정보")
    private LivingEnvironment livingEnvironment;

    @Schema(description = "최종목표 주택 여부")
    private Boolean isGoal;

    @Schema(description = "교통호재 목록")
    private List<TransportationResponse> transportations;

    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각")
    private LocalDateTime updatedAt;
}
