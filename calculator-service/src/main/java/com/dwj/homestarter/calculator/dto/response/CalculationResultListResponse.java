package com.dwj.homestarter.calculator.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 계산 결과 목록 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "계산 결과 목록 응답")
public class CalculationResultListResponse {

    /**
     * 계산 결과 목록
     */
    @Schema(description = "계산 결과 목록")
    private List<CalculationResultListItem> results;

    /**
     * 현재 페이지 번호
     */
    @Schema(description = "현재 페이지 번호", example = "0")
    private Integer page;

    /**
     * 페이지 크기
     */
    @Schema(description = "페이지 크기", example = "20")
    private Integer size;

    /**
     * 전체 결과 수
     */
    @Schema(description = "전체 결과 수", example = "100")
    private Long total;
}
