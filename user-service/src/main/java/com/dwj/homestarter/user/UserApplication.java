package com.dwj.homestarter.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * User Service 메인 애플리케이션
 *
 * 사용자 인증 및 기본정보 관리 서비스
 *
 * @author homestarter
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {
        "com.dwj.homestarter.user",
        "com.dwj.homestarter.common"
})
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.dwj.homestarter.user.repository.jpa")
@EntityScan(basePackages = "com.dwj.homestarter.user.repository.entity")
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
