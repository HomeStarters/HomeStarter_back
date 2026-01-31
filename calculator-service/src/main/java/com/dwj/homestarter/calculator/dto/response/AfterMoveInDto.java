package com.dwj.homestarter.calculator.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 입주 후 재무상태 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "입주 후 재무상태")
public class AfterMoveInDto {

    /**
     * 입주 후 자산 (원)
     */
    @Schema(description = "입주 후 자산 (원)", example = "50000000")
    private Long assets;

    /**
     * 월 지출 (원)
     */
    @Schema(description = "월 지출 (원)", example = "3000000")
    private Long monthlyExpenses;

    /**
     * 월 소득 (원)
     */
    @Schema(description = "월 소득 (원)", example = "5000000")
    private Long monthlyIncome;

    /**
     * 월 여유 자금 (원)
     */
    @Schema(description = "월 여유 자금 (원)", example = "2000000")
    private Long monthlyAvailableFunds;
}
