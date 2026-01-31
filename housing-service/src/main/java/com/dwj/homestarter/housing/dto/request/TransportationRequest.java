package com.dwj.homestarter.housing.dto.request;

import com.dwj.homestarter.housing.domain.enums.TransportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 교통호재 정보 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "교통호재 정보 요청")
public class TransportationRequest {

    /**
     * 교통수단 유형
     */
    @NotNull(message = "교통수단 유형은 필수입니다")
    @Schema(description = "교통수단 유형", example = "SUBWAY")
    private TransportType transportType;

    /**
     * 노선명
     */
    @Schema(description = "노선명", example = "2호선")
    private String lineName;

    /**
     * 역/정류장명
     */
    @NotBlank(message = "역/정류장명은 필수입니다")
    @Schema(description = "역/정류장명", example = "강남역")
    private String stationName;

    /**
     * 거리 (m)
     */
    @Min(value = 0, message = "거리는 0 이상이어야 합니다")
    @Schema(description = "거리 (m)", example = "500.50")
    private BigDecimal distance;

    /**
     * 도보 소요시간 (분)
     */
    @Min(value = 0, message = "도보 소요시간은 0 이상이어야 합니다")
    @Schema(description = "도보 소요시간 (분)", example = "7")
    private Integer walkingTime;

    /**
     * 출퇴근 시간 정보
     */
    @Valid
    @Schema(description = "출퇴근 시간 정보")
    private CommuteTimeRequest commuteTime;
}
