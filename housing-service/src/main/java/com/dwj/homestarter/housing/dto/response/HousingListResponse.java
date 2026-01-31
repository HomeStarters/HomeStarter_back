package com.dwj.homestarter.housing.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "주택 목록 응답")
public class HousingListResponse {
    @Schema(description = "주택 목록")
    private List<HousingListItem> housings;

    @Schema(description = "전체 개수")
    private Long totalCount;

    @Schema(description = "현재 페이지")
    private Integer currentPage;

    @Schema(description = "전체 페이지 수")
    private Integer totalPages;
}
