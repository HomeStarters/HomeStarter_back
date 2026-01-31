package com.dwj.homestarter.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {

    /**
     * 이름 (2자 이상, 한글/영문)
     */
    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
    private String name;

    /**
     * 이메일 주소
     */
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "유효한 이메일 형식이 아닙니다")
    private String email;

    /**
     * 휴대폰 번호 (하이픈 제외)
     */
    @NotBlank(message = "휴대폰 번호는 필수입니다")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "유효한 휴대폰 번호 형식이 아닙니다")
    private String phoneNumber;

    /**
     * 아이디 (5자 이상, 영문/숫자 조합)
     */
    @NotBlank(message = "아이디는 필수입니다")
    @Size(min = 5, max = 20, message = "아이디는 5자 이상 20자 이하여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문과 숫자만 가능합니다")
    private String userId;

    /**
     * 비밀번호 (8자 이상, 영문/숫자/특수문자 포함)
     */
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
    private String password;

    /**
     * 비밀번호 확인
     */
    @NotBlank(message = "비밀번호 확인은 필수입니다")
    private String passwordConfirm;

    /**
     * 이용약관 동의 여부
     */
    @NotNull(message = "이용약관 동의는 필수입니다")
    @AssertTrue(message = "이용약관에 동의해야 합니다")
    private Boolean agreeTerms;
}
