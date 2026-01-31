package com.dwj.homestarter.housing.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 주소 정보 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "주소 정보 요청")
public class AddressRequest {

    /**
     * 전체 주소
     */
    @NotBlank(message = "전체 주소는 필수입니다")
    @Schema(description = "전체 주소", example = "서울특별시 강남구 테헤란로 123")
    private String fullAddress;

    /**
     * 도로명 주소
     */
    @Schema(description = "도로명 주소", example = "서울특별시 강남구 테헤란로 123")
    private String roadAddress;

    /**
     * 지번 주소
     */
    @Schema(description = "지번 주소", example = "서울특별시 강남구 역삼동 123-45")
    private String jibunAddress;

    /**
     * 위도
     */
    @Schema(description = "위도", example = "37.5012345")
    private BigDecimal latitude;

    /**
     * 경도
     */
    @Schema(description = "경도", example = "127.0398765")
    private BigDecimal longitude;
}
