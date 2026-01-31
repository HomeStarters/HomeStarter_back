package com.dwj.homestarter.calculator.config;

import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign Client 설정
 * 외부 마이크로서비스와의 통신을 위한 Feign Client 활성화
 */
@Configuration
@EnableFeignClients(basePackages = "com.dwj.homestarter.calculator.service.client")
public class FeignClientConfig {

    /**
     * Authorization 헤더 전파 인터셉터
     * 원본 요청의 JWT 토큰을 Feign Client 호출 시 전달
     */
    @Bean
    public RequestInterceptor authorizationRequestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String authorization = attributes.getRequest().getHeader("Authorization");
                if (authorization != null) {
                    requestTemplate.header("Authorization", authorization);
                }
            }
        };
    }
}
