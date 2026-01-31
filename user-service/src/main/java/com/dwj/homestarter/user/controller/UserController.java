package com.dwj.homestarter.user.controller;

import com.dwj.homestarter.common.dto.ApiResponse;
import com.dwj.homestarter.user.dto.request.UserLoginRequest;
import com.dwj.homestarter.user.dto.request.UserProfileUpdateRequest;
import com.dwj.homestarter.user.dto.request.UserRegisterRequest;
import com.dwj.homestarter.user.dto.response.UserLoginResponse;
import com.dwj.homestarter.user.dto.response.UserProfileResponse;
import com.dwj.homestarter.user.dto.response.UserRegisterResponse;
import com.dwj.homestarter.user.service.UserService;
import com.dwj.homestarter.user.service.jwt.JwtTokenProvider;
import com.dwj.homestarter.user.service.jwt.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 컨트롤러
 *
 * 사용자 인증 및 기본정보 관리 API 제공
 *
 * @author homestarter
 * @since 1.0.0
 */
@Tag(name = "User Service", description = "사용자 인증 및 기본정보 관리 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     *
     * @param request 회원가입 요청 DTO
     * @return 회원가입 응답
     */
    @Operation(summary = "회원가입", description = "신규 사용자 회원가입을 처리합니다")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserRegisterResponse>> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        UserRegisterResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다", response));
    }

    /**
     * 로그인
     *
     * @param request 로그인 요청 DTO
     * @return 로그인 응답 (JWT 토큰 포함)
     */
    @Operation(summary = "로그인", description = "사용자 로그인을 처리하고 JWT 토큰을 발급합니다")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> loginUser(@Valid @RequestBody UserLoginRequest request) {
        UserLoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다", response));
    }

    /**
     * 로그아웃
     *
     * @param request HTTP 요청
     * @param principal 인증된 사용자 정보
     * @return 로그아웃 응답
     */
    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리하고 JWT 토큰을 무효화합니다")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logoutUser(HttpServletRequest request,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        String accessToken = jwtTokenProvider.resolveToken(request);
        userService.logout(accessToken, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다"));
    }

    /**
     * 기본정보 조회
     *
     * @param principal 인증된 사용자 정보
     * @return 프로필 응답
     */
    @Operation(summary = "기본정보 조회", description = "로그인한 사용자의 기본정보를 조회합니다")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(@AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse response = userService.getUserProfile(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("기본정보 조회에 성공했습니다", response));
    }

    /**
     * 기본정보 수정
     *
     * @param request 프로필 수정 요청 DTO
     * @param principal 인증된 사용자 정보
     * @return 프로필 응답
     */
    @Operation(summary = "기본정보 수정", description = "로그인한 사용자의 기본정보를 수정합니다 (생년월일, 성별은 수정 불가)")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserProfile(@Valid @RequestBody UserProfileUpdateRequest request,
                                                                               @AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse response = userService.updateUserProfile(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("기본정보가 업데이트되었습니다", response));
    }
}
