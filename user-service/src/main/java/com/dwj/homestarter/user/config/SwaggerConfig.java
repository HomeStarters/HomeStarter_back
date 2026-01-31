package com.dwj.homestarter.user.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 설정
 *
 * API 문서 자동 생성 설정
 *
 * @author homestarter
 * @since 1.0.0
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * OpenAPI 3.0 설정
     *
     * @return OpenAPI
     */
    @Bean
    public OpenAPI openAPI() {
        // JWT 인증 스키마 정의
        String jwtSchemeName = "bearerAuth";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT 토큰을 입력하세요 (Bearer 접두사 제외)")
                );

        return new OpenAPI()
                .info(new Info()
                        .title("내집마련 도우미 플랫폼 - User Service API")
                        .description("사용자 인증 및 기본정보 관리 API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("홈스타터 팀")
                                .email("support@homestarter.com")
                                .url("https://homestarter.com")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                        )
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("로컬 개발 서버"),
                        new Server()
                                .url("https://api.homestarter.com")
                                .description("운영 서버")
                ))
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
