package com.dwj.homestarter.housing.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 출퇴근 시간 정보 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "출퇴근 시간 정보 요청")
public class CommuteTimeRequest {

    /**
     * 본인 출근 소요시간 (9시 이전 도착, 분)
     */
    @Min(value = 0, message = "출근 소요시간은 0 이상이어야 합니다")
    @Schema(description = "본인 출근 소요시간 (9시 이전 도착, 분)", example = "45")
    private Integer selfBefore9am;

    /**
     * 본인 퇴근 소요시간 (18시 이후 출발, 분)
     */
    @Min(value = 0, message = "퇴근 소요시간은 0 이상이어야 합니다")
    @Schema(description = "본인 퇴근 소요시간 (18시 이후 출발, 분)", example = "50")
    private Integer selfAfter6pm;

    /**
     * 배우자 출근 소요시간 (9시 이전 도착, 분)
     */
    @Min(value = 0, message = "배우자 출근 소요시간은 0 이상이어야 합니다")
    @Schema(description = "배우자 출근 소요시간 (9시 이전 도착, 분)", example = "40")
    private Integer spouseBefore9am;

    /**
     * 배우자 퇴근 소요시간 (18시 이후 출발, 분)
     */
    @Min(value = 0, message = "배우자 퇴근 소요시간은 0 이상이어야 합니다")
    @Schema(description = "배우자 퇴근 소요시간 (18시 이후 출발, 분)", example = "45")
    private Integer spouseAfter6pm;
}
