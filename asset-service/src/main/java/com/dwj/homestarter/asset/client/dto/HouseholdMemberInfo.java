package com.dwj.homestarter.asset.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * user-service 가구원 정보 DTO
 * user-service의 HouseholdMemberResponse.MemberInfo에 대응
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdMemberInfo {
    private String userId;
    private String name;
    private String email;
    private String role;
}
