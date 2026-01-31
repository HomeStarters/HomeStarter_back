package com.dwj.homestarter.asset.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 설정
 * Asset Service API 문서화를 위한 설정
 */
@Configuration
public class SwaggerConfig {

    /**
     * OpenAPI 설정
     * API 정보, 서버 정보, 보안 스키마 설정
     *
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addServersItem(new Server()
                        .url("http://localhost:8082")
                        .description("Local Development"))
                .addServersItem(new Server()
                        .url("{protocol}://{host}:{port}")
                        .description("Custom Server")
                        .variables(new io.swagger.v3.oas.models.servers.ServerVariables()
                                .addServerVariable("protocol", new io.swagger.v3.oas.models.servers.ServerVariable()
                                        ._default("http")
                                        .description("Protocol (http or https)")
                                        .addEnumItem("http")
                                        .addEnumItem("https"))
                                .addServerVariable("host", new io.swagger.v3.oas.models.servers.ServerVariable()
                                        ._default("localhost")
                                        .description("Server host"))
                                .addServerVariable("port", new io.swagger.v3.oas.models.servers.ServerVariable()
                                        ._default("8082")
                                        .description("Server port"))))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    /**
     * API 정보 설정
     * API 제목, 설명, 버전, 연락처 정보
     *
     * @return API 정보 객체
     */
    private Info apiInfo() {
        return new Info()
                .title("Asset Service API")
                .description("내집마련 도우미 플랫폼 - 자산정보 관리 서비스 API<br>" +
                            "본인 및 배우자의 자산, 대출, 월소득, 월지출 정보를 관리하는 서비스입니다.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Backend Development Team")
                        .email("backend@home-starter.com"));
    }

    /**
     * JWT 보안 스키마 설정
     * Bearer 토큰 인증 방식
     *
     * @return 보안 스키마 객체
     */
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}
