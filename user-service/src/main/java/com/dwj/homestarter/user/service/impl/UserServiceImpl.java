package com.dwj.homestarter.user.service.impl;

import com.dwj.homestarter.common.exception.BusinessException;
import com.dwj.homestarter.common.exception.NotFoundException;
import com.dwj.homestarter.common.exception.UnauthorizedException;
import com.dwj.homestarter.common.exception.ValidationException;
import com.dwj.homestarter.user.dto.request.*;
import com.dwj.homestarter.user.dto.response.*;
import com.dwj.homestarter.user.repository.entity.*;
import com.dwj.homestarter.user.repository.jpa.UserProfileRepository;
import com.dwj.homestarter.user.repository.jpa.UserRepository;
import com.dwj.homestarter.user.service.UserService;
import com.dwj.homestarter.user.service.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.regex.Pattern;

/**
 * 사용자 서비스 구현체
 *
 * 사용자 관련 비즈니스 로직 구현
 *
 * @author homestarter
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final String LOGIN_FAIL_KEY_PREFIX = "login:fail:";
    private static final String TOKEN_BLACKLIST_KEY_PREFIX = "token:blacklist:";
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOGIN_LOCK_DURATION = Duration.ofMinutes(30);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$");

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional
    public UserRegisterResponse register(UserRegisterRequest request) {
        // 아이디 중복 확인
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new BusinessException("USER_001", "이미 사용 중인 아이디입니다");
        }

        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("USER_002", "이미 사용 중인 이메일입니다");
        }

        // 비밀번호 확인
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new BusinessException("USER_003", "비밀번호가 일치하지 않습니다");
        }

        // 비밀번호 강도 확인
        if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            throw new BusinessException("USER_004", "비밀번호는 8자 이상, 영문/숫자/특수문자를 포함해야 합니다");
        }

        // 사용자 엔티티 생성 및 저장
        UserEntity userEntity = UserEntity.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();
        UserProfileEntity userProfileEntity = UserProfileEntity.builder()
                .userId(request.getUserId())
                .build();

        userRepository.save(userEntity);
        userProfileRepository.save(userProfileEntity);

        log.info("User registered successfully: {}", request.getUserId());

        return UserRegisterResponse.builder()
                .userId(userEntity.getUserId())
                .email(userEntity.getEmail())
                .build();
    }

    @Override
    @Transactional
    public UserLoginResponse login(UserLoginRequest request) {
        // 로그인 실패 횟수 확인
        String failKey = LOGIN_FAIL_KEY_PREFIX + request.getUserId();
        String failCount = redisTemplate.opsForValue().get(failKey);

        if (failCount != null && Integer.parseInt(failCount) >= MAX_LOGIN_ATTEMPTS) {
            throw new UnauthorizedException("AUTH_002", "5회 연속 로그인 실패로 계정이 30분간 잠겼습니다");
        }

        // 사용자 조회
        UserEntity user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new UnauthorizedException("AUTH_001", "아이디 또는 비밀번호를 확인해주세요"));

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // 로그인 실패 횟수 증가
            incrementLoginFailCount(failKey);
            throw new UnauthorizedException("AUTH_001", "아이디 또는 비밀번호를 확인해주세요");
        }

        // 로그인 성공 - 실패 횟수 초기화
        redisTemplate.delete(failKey);

        // 마지막 로그인 시간 업데이트
        user.updateLastLoginAt();

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getName(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        log.info("User logged in successfully: {}", request.getUserId());

        return UserLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn((int) jwtTokenProvider.getAccessTokenValidity())
                .user(UserBasicInfo.builder()
                        .userId(user.getUserId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .isAdmin("ADMIN".equals(user.getRole()))
                        .build())
                .build();
    }

    @Override
    @Transactional
    public void logout(String accessToken, String userId) {
        // 토큰 블랙리스트에 등록
        String blacklistKey = TOKEN_BLACKLIST_KEY_PREFIX + accessToken;
        long expirationTime = jwtTokenProvider.getExpirationDate(accessToken).getTime() - System.currentTimeMillis();

        if (expirationTime > 0) {
            redisTemplate.opsForValue().set(blacklistKey, userId, Duration.ofMillis(expirationTime));
        }

        log.info("User logged out successfully: {}", userId);
    }

    @Override
    public UserProfileResponse getUserProfile(String userId) {
        // 사용자 조회
        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        // 프로필 조회
        UserProfileEntity profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("USER_005", "기본정보가 등록되지 않았습니다"));

        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .birthDate(profile.getBirthDate())
                .gender(profile.getGender())
                .currentAddress(profile.getCurrentAddress())
                .userWorkplaceAddress(profile.getUserWorkplaceAddress())
                .spouseWorkplaceAddress(profile.getSpouseWorkplaceAddress())
                .withholdingTaxSalary(profile.getWithholdingTaxSalary())
//                .currentAddress(toAddressResponse(profile.getCurrentAddress()))
//                .userWorkplaceAddress(toAddressResponse(profile.getUserWorkplaceAddress()))
//                .spouseWorkplaceAddress(toAddressResponse(profile.getSpouseWorkplaceAddress()))
                .investmentPropensity(profile.getInvestmentPropensity())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(String userId, UserProfileUpdateRequest request) {
        // 프로필 조회
        UserProfileEntity profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("USER_005", "기본정보가 등록되지 않았습니다"));

        // 프로필 업데이트 (JPA 더티 체킹 활용)
        profile.updateProfile(
                request.getCurrentAddress(),
                request.getUserWorkplaceAddress(),
                request.getSpouseWorkplaceAddress(),
                request.getBirthDate(),
                request.getGender(),
                request.getInvestmentPropensity(),
                request.getWithholdingTaxSalary()
        );

//        // 프로필 업데이트 (JPA 더티 체킹 활용)
//        profile.updateProfile(
//                toAddressEmbeddable(request.getCurrentAddress()),
//                toAddressEmbeddable(request.getUserWorkplaceAddress()),
//                toAddressEmbeddable(request.getSpouseWorkplaceAddress()),
//                request.getInvestmentPropensity()
//        );

        log.info("User profile updated successfully: {}", userId);

        return getUserProfile(userId);
    }

    @Override
    public boolean existsByUserId(String userId) {
        return userRepository.existsByUserId(userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 로그인 실패 횟수 증가
     *
     * @param failKey Redis 키
     */
    private void incrementLoginFailCount(String failKey) {
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count == 1) {
            redisTemplate.expire(failKey, LOGIN_LOCK_DURATION);
        }
    }

    /**
     * AddressRequest를 AddressEmbeddable로 변환
     *
     * @param request AddressRequest
     * @return AddressEmbeddable
     */
    private AddressEmbeddable toAddressEmbeddable(AddressRequest request) {
        if (request == null) {
            return null;
        }
        return AddressEmbeddable.builder()
                .roadAddress(request.getRoadAddress())
                .jibunAddress(request.getJibunAddress())
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
    }

    /**
     * AddressEmbeddable을 AddressResponse로 변환
     *
     * @param address AddressEmbeddable
     * @return AddressResponse
     */
    private AddressResponse toAddressResponse(AddressEmbeddable address) {
        if (address == null) {
            return null;
        }
        return AddressResponse.builder()
                .roadAddress(address.getRoadAddress())
                .jibunAddress(address.getJibunAddress())
                .postalCode(address.getPostalCode())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .build();
    }
}
