package com.dwj.homestarter.asset.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 가구원별 자산 응답 DTO
 * 특정 가구원의 기본정보 + 자산정보를 포함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseholdMemberAssetResponse {

    /**
     * 가구원 사용자 ID
     */
    private String userId;

    /**
     * 가구원 이름
     */
    private String userName;

    /**
     * 가구 내 역할 (OWNER/MEMBER)
     */
    private String role;

    /**
     * 해당 가구원의 자산정보 (본인+배우자 포함)
     */
    private AssetListResponse assets;
}
