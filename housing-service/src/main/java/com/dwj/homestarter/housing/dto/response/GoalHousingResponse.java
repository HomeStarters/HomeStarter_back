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
@Schema(description = "최종목표 주택 설정 응답")
public class GoalHousingResponse {
    @Schema(description = "주택 ID")
    private Long housingId;

    @Schema(description = "주택명")
    private String housingName;

    @Schema(description = "메시지")
    private String message;
}
