package com.dwj.homestarter.housing.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주택 삭제 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주택 삭제 응답")
public class HousingDeleteResponse {

    @Schema(description = "삭제된 주택 ID", example = "1")
    private Long housingId;

    @Schema(description = "삭제된 주택 이름", example = "래미안 원펜타스")
    private String housingName;

    @Schema(description = "삭제 메시지", example = "주택이 삭제되었습니다")
    private String message;
}
