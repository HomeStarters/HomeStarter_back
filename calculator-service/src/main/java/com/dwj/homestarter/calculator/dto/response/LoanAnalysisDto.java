package com.dwj.homestarter.calculator.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 대출 분석 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "대출 분석")
public class LoanAnalysisDto {

    /**
     * 계산된 LTV (%)
     */
    @Schema(description = "계산된 LTV (%)", example = "70.0")
    private Double ltv;

    /**
     * 계산된 DTI (%)
     */
    @Schema(description = "계산된 DTI (%)", example = "40.0")
    private Double dti;

    /**
     * 계산된 DSR (%)
     */
    @Schema(description = "계산된 DSR (%)", example = "35.0")
    private Double dsr;

    /**
     * LTV 한도 (%)
     */
    @Schema(description = "LTV 한도 (%)", example = "70.0")
    private Double ltvLimit;

    /**
     * DTI 한도 (%)
     */
    @Schema(description = "DTI 한도 (%)", example = "40.0")
    private Double dtiLimit;

    /**
     * DSR 한도 (%)
     */
    @Schema(description = "DSR 한도 (%)", example = "40.0")
    private Double dsrLimit;

    /**
     * 충족 여부
     */
    @Schema(description = "충족 여부", example = "true")
    private Boolean isEligible;

    /**
     * 미충족 사유 목록
     */
    @Schema(description = "미충족 사유 목록")
    private List<String> ineligibilityReasons;

    /**
     * 월 상환액 (원)
     */
    @Schema(description = "월 상환액 (원)", example = "1500000")
    private Long monthlyPayment;
}
