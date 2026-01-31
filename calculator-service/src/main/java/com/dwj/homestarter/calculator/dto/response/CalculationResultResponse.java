package com.dwj.homestarter.calculator.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 계산 결과 상세 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "계산 결과 상세 응답")
public class CalculationResultResponse {

    /**
     * 계산 결과 ID
     */
    @Schema(description = "계산 결과 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    /**
     * 사용자 ID
     */
    @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private String userId;

    /**
     * 주택 ID
     */
    @Schema(description = "주택 ID", example = "550e8400-e29b-41d4-a716-446655440002")
    private String housingId;

    /**
     * 주택 이름
     */
    @Schema(description = "주택 이름", example = "아파트A")
    private String housingName;

    /**
     * 입주 예정일
     */
    @Schema(description = "입주 예정일", example = "2025-12-30")
    private LocalDate moveInDate;

    /**
     * 대출상품 ID
     */
    @Schema(description = "대출상품 ID", example = "550e8400-e29b-41d4-a716-446655440003")
    private String loanProductId;

    /**
     * 대출상품 이름
     */
    @Schema(description = "대출상품 이름", example = "주택담보대출A")
    private String loanProductName;

    /**
     * 대출 금액 (원)
     */
    @Schema(description = "대출 금액 (원)", example = "300000000")
    private Long loanAmount;

    /**
     * 계산 일시
     */
    @Schema(description = "계산 일시", example = "2025-12-30T10:00:00")
    private LocalDateTime calculatedAt;

    /**
     * 재무 현황
     */
    @Schema(description = "재무 현황")
    private FinancialStatusDto financialStatus;

    /**
     * 대출 분석
     */
    @Schema(description = "대출 분석")
    private LoanAnalysisDto loanAnalysis;

    /**
     * 입주 후 재무상태
     */
    @Schema(description = "입주 후 재무상태")
    private AfterMoveInDto afterMoveIn;

    /**
     * 상태 (ELIGIBLE, INELIGIBLE)
     */
    @Schema(description = "상태", example = "ELIGIBLE")
    private String status;
}
