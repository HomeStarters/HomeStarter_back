package com.dwj.homestarter.calculator.service.client;

import com.dwj.homestarter.calculator.dto.external.wrapper.ApiResponse;
import com.dwj.homestarter.calculator.dto.external.wrapper.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * User Service Feign 클라이언트
 */
@FeignClient(name = "user-service", url = "${service.url.user}")
public interface UserServiceClient {

    /**
     * 사용자 프로필 조회 (JWT 토큰 기반 인증)
     *
     * @return ApiResponse 래퍼로 감싼 사용자 프로필
     */
    @GetMapping("/users/profile")
    ApiResponse<UserProfileResponse> getUserProfile();
}
