package com.dwj.homestarter.asset.client;

import com.dwj.homestarter.asset.client.dto.HouseholdMembersData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Optional;

/**
 * User Service REST 클라이언트
 * user-service의 가구원 관련 API를 호출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final WebClient userServiceWebClient;

    /**
     * 가구원 목록 조회
     * user-service의 GET /users/household/members API 호출
     *
     * @param token JWT 토큰 (Bearer 포함)
     * @return 가구원 목록 (호출 실패 시 빈 Optional)
     */
    public Optional<HouseholdMembersData> getHouseholdMembers(String token) {
        try {
            log.info("user-service 가구원 목록 조회 요청");

            Map<String, Object> response = userServiceWebClient.get()
                    .uri("/users/household/members")
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null || !Boolean.TRUE.equals(response.get("success"))) {
                log.warn("user-service 가구원 목록 조회 실패 - 응답: {}", response);
                return Optional.empty();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data == null) {
                return Optional.empty();
            }

            return Optional.of(parseHouseholdMembersData(data));

        } catch (Exception e) {
            log.warn("user-service 가구원 목록 조회 중 오류 발생 - {}", e.getMessage());
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private HouseholdMembersData parseHouseholdMembersData(Map<String, Object> data) {
        String householdId = (String) data.get("householdId");
        java.util.List<com.dwj.homestarter.asset.client.dto.HouseholdMemberInfo> members = new java.util.ArrayList<>();

        java.util.List<Map<String, Object>> memberList = (java.util.List<Map<String, Object>>) data.get("members");
        if (memberList != null) {
            for (Map<String, Object> m : memberList) {
                members.add(new com.dwj.homestarter.asset.client.dto.HouseholdMemberInfo(
                        (String) m.get("userId"),
                        (String) m.get("name"),
                        (String) m.get("email"),
                        (String) m.get("role")
                ));
            }
        }

        return new HouseholdMembersData(householdId, members);
    }
}
