package com.dwj.homestarter.roadmap.client;

import com.dwj.homestarter.roadmap.client.dto.UserProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * User Service Feign Client
 * 사용자 프로필 정보 조회
 */
@FeignClient(
    name = "user-service",
    url = "${feign.client.url.user-service:http://localhost:8081}"
)
public interface UserClient {

    /**
     * 사용자 프로필 조회
     *
     * @param authorization JWT 토큰
     * @return 사용자 프로필 정보
     */
    @GetMapping("/users/profile")
    UserProfileDto getUserProfile(@RequestHeader("Authorization") String authorization);
}
