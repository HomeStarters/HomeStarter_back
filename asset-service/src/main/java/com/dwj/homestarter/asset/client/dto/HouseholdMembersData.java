package com.dwj.homestarter.asset.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * user-service 가구원 목록 응답 DTO
 * user-service의 HouseholdMemberResponse에 대응
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdMembersData {
    private String householdId;
    private List<HouseholdMemberInfo> members;
}
