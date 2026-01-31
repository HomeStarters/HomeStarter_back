package com.dwj.homestarter.asset.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 자산정보 목록 응답 DTO
 * 본인 + 배우자 자산정보와 합산 정보를 포함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetListResponse {

    /**
     * 자산정보 목록 (본인 + 배우자)
     */
    private List<AssetResponse> assets;

    /**
     * 가구 전체 자산 요약
     */
    private CombinedAssetSummaryDto combinedSummary;
}
