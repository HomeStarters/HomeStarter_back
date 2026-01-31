package com.dwj.homestarter.asset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Asset Service 메인 애플리케이션
 *
 * 자산정보 관리 서비스
 *
 * @author homestarter
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {
        "com.dwj.homestarter.asset",
        "com.dwj.homestarter.common"
})
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.dwj.homestarter.asset.repository.jpa")
public class AssetApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssetApplication.class, args);
    }
}
