package com.dwj.homestarter.roadmap.config.jwt;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 인증된 사용자 정보
 *
 * JWT 토큰에서 추출된 사용자 정보를 담는 Principal 객체
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@Builder
@RequiredArgsConstructor
public class UserPrincipal {

    /**
     * 사용자 고유 ID
     */
    private final String userId;

    /**
     * 사용자명
     */
    private final String username;

    /**
     * 사용자 권한
     */
    private final String authority;
}
