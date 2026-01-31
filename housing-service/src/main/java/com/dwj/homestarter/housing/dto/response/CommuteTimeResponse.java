package com.dwj.homestarter.housing.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "출퇴근 시간 정보 응답")
public class CommuteTimeResponse {
    @Schema(description = "본인 출근 소요시간 (9시 이전 도착, 분)")
    private Integer selfBefore9am;

    @Schema(description = "본인 퇴근 소요시간 (18시 이후 출발, 분)")
    private Integer selfAfter6pm;

    @Schema(description = "배우자 출근 소요시간 (9시 이전 도착, 분)")
    private Integer spouseBefore9am;

    @Schema(description = "배우자 퇴근 소요시간 (18시 이후 출발, 분)")
    private Integer spouseAfter6pm;
}
