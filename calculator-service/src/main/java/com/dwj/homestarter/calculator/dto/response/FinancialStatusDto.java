package com.dwj.homestarter.calculator.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 재무 현황 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "재무 현황")
public class FinancialStatusDto {

    /**
     * 현재 순자산 (원)
     */
    @Schema(description = "현재 순자산 (원)", example = "100000000")
    private Long currentAssets;

    /**
     * 예상자산 (원)
     */
    @Schema(description = "예상자산 (원)", example = "150000000")
    private Long estimatedAssets;

    /**
     * 대출필요금액 (원)
     */
    @Schema(description = "대출필요금액 (원)", example = "300000000")
    private Long loanRequired;
}
