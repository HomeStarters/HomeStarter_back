package com.dwj.homestarter.asset.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 가구 전체 자산 응답 DTO
 * 가구에 속한 모든 구성원의 자산정보와 가구 합산 정보를 포함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseholdAssetResponse {

    /**
     * 가구 ID (가구 미가입 시 null)
     */
    private String householdId;

    /**
     * 가구원별 자산정보 목록
     */
    private List<HouseholdMemberAssetResponse> members;

    /**
     * 가구 전체 자산 합산 정보
     */
    private CombinedAssetSummaryDto householdSummary;
}
