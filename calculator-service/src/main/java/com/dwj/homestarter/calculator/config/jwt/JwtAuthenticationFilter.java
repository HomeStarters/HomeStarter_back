package com.dwj.homestarter.calculator.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 인증 필터
 *
 * HTTP 요청에서 JWT 토큰을 추출하여 인증 처리
 *
 * @author homestarter
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_BLACKLIST_KEY_PREFIX = "token:blacklist:";

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // JWT 토큰 추출
            String jwt = jwtTokenProvider.resolveToken(request);

            if (StringUtils.hasText(jwt)) {
                // 토큰 블랙리스트 확인
                String blacklistKey = TOKEN_BLACKLIST_KEY_PREFIX + jwt;
                if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                    log.debug("Token is blacklisted: {}", jwt);
                    filterChain.doFilter(request, response);
                    return;
                }

                // 토큰 검증
                if (jwtTokenProvider.validateToken(jwt)) {
                    // 사용자 정보 추출
                    String userId = jwtTokenProvider.getUserId(jwt);
                    String username = jwtTokenProvider.getUsername(jwt);
                    String authority = jwtTokenProvider.getAuthority(jwt);

                    // UserPrincipal 생성
                    UserPrincipal principal = UserPrincipal.builder()
                            .userId(userId)
                            .username(username)
                            .authority(authority)
                            .build();

                    // Authentication 객체 생성
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + authority))
                    );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // SecurityContext에 인증 정보 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Set authentication for user: {}", userId);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
