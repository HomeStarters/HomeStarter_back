package com.dwj.homestarter.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 응답 데이터 DTO
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponse {

    /**
     * JWT 액세스 토큰
     */
    private String accessToken;

    /**
     * JWT 리프레시 토큰
     */
    private String refreshToken;

    /**
     * 토큰 타입
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * 토큰 만료 시간 (초)
     */
    private Integer expiresIn;

    /**
     * 사용자 기본 정보
     */
    private UserBasicInfo user;
}
