package com.dwj.homestarter.user.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 가구원 목록 응답 DTO
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseholdMemberResponse {

    private String householdId;
    private List<MemberInfo> members;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberInfo {
        private String userId;
        private String name;
        private String email;
        private String role;
        private LocalDateTime joinedAt;
    }
}
