package com.dwj.homestarter.housing.dto.request;

import com.dwj.homestarter.housing.domain.enums.HousingType;
import com.dwj.homestarter.housing.domain.model.ComplexInfo;
import com.dwj.homestarter.housing.domain.model.LivingEnvironment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 주택 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "주택 수정 요청")
public class HousingUpdateRequest {

    @NotBlank(message = "주택명은 필수입니다")
    @Schema(description = "주택명", example = "래미안 강남 포레스트")
    private String housingName;

    @NotNull(message = "주택 유형은 필수입니다")
    @Schema(description = "주택 유형", example = "APARTMENT")
    private HousingType housingType;

    @NotNull(message = "가격은 필수입니다")
    @Min(value = 1, message = "가격은 0보다 커야 합니다")
    @Schema(description = "가격 (원)", example = "850000000")
    private BigDecimal price;

    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "입주희망년월 형식은 YYYY-MM이어야 합니다")
    @Schema(description = "입주희망년월 (YYYY-MM)", example = "2025-03")
    private String moveInDate;

    @Schema(description = "준공일", example = "2024-12-31")
    private LocalDate completionDate;

    @NotNull(message = "주소는 필수입니다")
    @Schema(description = "주소 정보")
    private String address;

//    @NotNull(message = "주소는 필수입니다")
//    @Valid
//    @Schema(description = "주소 정보")
//    private AddressRequest address;

    @Schema(description = "단지 정보")
    private ComplexInfo complexInfo;

    @Schema(description = "생활환경 정보")
    private LivingEnvironment livingEnvironment;

    @Valid
    @Schema(description = "교통호재 목록")
    private List<TransportationRequest> transportations;
}
