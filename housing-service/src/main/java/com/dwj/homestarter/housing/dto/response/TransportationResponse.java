package com.dwj.homestarter.housing.dto.response;

import com.dwj.homestarter.housing.domain.enums.TransportType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "교통호재 정보 응답")
public class TransportationResponse {
    @Schema(description = "교통호재 ID")
    private Long id;

    @Schema(description = "교통수단 유형")
    private TransportType transportType;

    @Schema(description = "노선명")
    private String lineName;

    @Schema(description = "역/정류장명")
    private String stationName;

    @Schema(description = "거리 (m)")
    private BigDecimal distance;

    @Schema(description = "도보 소요시간 (분)")
    private Integer walkingTime;

    @Schema(description = "출퇴근 시간 정보")
    private CommuteTimeResponse commuteTime;
}
