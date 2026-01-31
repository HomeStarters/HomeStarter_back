package com.dwj.homestarter.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 기본 정보 DTO
 *
 * 로그인 응답에 포함되는 간단한 사용자 정보
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicInfo {

    /**
     * 사용자 아이디
     */
    private String userId;

    /**
     * 이름
     */
    private String name;

    /**
     * 이메일
     */
    private String email;

    /**
     * 관리자 여부
     */
    private Boolean isAdmin;
}
