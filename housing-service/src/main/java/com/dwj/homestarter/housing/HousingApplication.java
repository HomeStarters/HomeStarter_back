package com.dwj.homestarter.housing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Housing Service 메인 애플리케이션
 *
 * 주택 정보 관리 서비스
 *
 * @author homestarter
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {
        "com.dwj.homestarter.housing",
        "com.dwj.homestarter.common"
})
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.dwj.homestarter.housing.repository")
public class HousingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HousingApplication.class, args);
    }
}
