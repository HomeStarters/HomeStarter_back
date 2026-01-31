package com.dwj.homestarter.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 응답 데이터 DTO
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterResponse {

    /**
     * 사용자 아이디
     */
    private String userId;

    /**
     * 이메일
     */
    private String email;
}
