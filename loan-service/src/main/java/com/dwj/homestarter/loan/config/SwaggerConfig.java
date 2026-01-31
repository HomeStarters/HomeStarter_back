package com.dwj.homestarter.loan.config;

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
 *
 * Loan Service API 문서화를 위한 설정
 *
 * @author homestarter
 * @since 1.0.0
 */
@Configuration
public class SwaggerConfig {

    /**
     * OpenAPI 설정
     *
     * @return OpenAPI 객체
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addServersItem(new Server()
                        .url("http://localhost:8083")
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
                                        ._default("8083")
                                        .description("Server port"))))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    /**
     * API 정보 설정
     *
     * @return Info 객체
     */
    private Info apiInfo() {
        return new Info()
                .title("Loan Service API")
                .description("대출상품 정보 관리 서비스 API\n" +
                        "- 사용자: 대출상품 목록/상세 조회, 필터링/정렬/검색\n" +
                        "- 관리자: 대출상품 등록/수정/삭제")
                .version("1.0.0")
                .contact(new Contact()
                        .name("HomeStarter Development Team")
                        .email("dev@homestarter.com"));
    }

    /**
     * Security Scheme 설정
     *
     * @return SecurityScheme 객체
     */
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}
