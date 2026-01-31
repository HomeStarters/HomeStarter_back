package com.dwj.homestarter.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequest {

    /**
     * 아이디
     */
    @NotBlank(message = "아이디는 필수입니다")
    private String userId;

    /**
     * 비밀번호
     */
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    /**
     * 로그인 유지 여부
     */
    @Builder.Default
    private Boolean rememberMe = false;
}
