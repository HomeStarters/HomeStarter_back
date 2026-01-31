package com.dwj.homestarter.user.service;

import com.dwj.homestarter.user.dto.request.UserLoginRequest;
import com.dwj.homestarter.user.dto.request.UserProfileUpdateRequest;
import com.dwj.homestarter.user.dto.request.UserRegisterRequest;
import com.dwj.homestarter.user.dto.response.UserLoginResponse;
import com.dwj.homestarter.user.dto.response.UserProfileResponse;
import com.dwj.homestarter.user.dto.response.UserRegisterResponse;

/**
 * 사용자 서비스 인터페이스
 *
 * 사용자 관련 비즈니스 로직을 정의
 *
 * @author homestarter
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 회원가입 처리
     *
     * @param request 회원가입 요청 DTO
     * @return 회원가입 응답 DTO
     */
    UserRegisterResponse register(UserRegisterRequest request);

    /**
     * 로그인 처리 및 토큰 발급
     *
     * @param request 로그인 요청 DTO
     * @return 로그인 응답 DTO (JWT 토큰 포함)
     */
    UserLoginResponse login(UserLoginRequest request);

    /**
     * 로그아웃 처리 및 토큰 무효화
     *
     * @param accessToken Access Token
     * @param userId 사용자 아이디
     */
    void logout(String accessToken, String userId);

    /**
     * 프로필 조회
     *
     * @param userId 사용자 아이디
     * @return 프로필 응답 DTO
     */
    UserProfileResponse getUserProfile(String userId);

    /**
     * 프로필 수정
     *
     * @param userId 사용자 아이디
     * @param request 프로필 수정 요청 DTO
     * @return 프로필 응답 DTO
     */
    UserProfileResponse updateUserProfile(String userId, UserProfileUpdateRequest request);

    /**
     * 아이디 중복 확인
     *
     * @param userId 사용자 아이디
     * @return 중복 여부
     */
    boolean existsByUserId(String userId);

    /**
     * 이메일 중복 확인
     *
     * @param email 이메일
     * @return 중복 여부
     */
    boolean existsByEmail(String email);
}
