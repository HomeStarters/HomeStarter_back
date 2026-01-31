package com.dwj.homestarter.calculator.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 계산 결과 목록 항목 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "계산 결과 목록 항목")
public class CalculationResultListItem {

    /**
     * 계산 결과 ID
     */
    @Schema(description = "계산 결과 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    /**
     * 주택 이름
     */
    @Schema(description = "주택 이름", example = "아파트A")
    private String housingName;

    /**
     * 대출상품 이름
     */
    @Schema(description = "대출상품 이름", example = "주택담보대출A")
    private String loanProductName;

    /**
     * 계산 일시
     */
    @Schema(description = "계산 일시", example = "2025-12-30T10:00:00")
    private LocalDateTime calculatedAt;

    /**
     * 상태 (ELIGIBLE, INELIGIBLE)
     */
    @Schema(description = "상태", example = "ELIGIBLE")
    private String status;

    /**
     * 여유 자금 (원)
     */
    @Schema(description = "여유 자금 (원)", example = "2000000")
    private Long monthlyAvailableFunds;
}
