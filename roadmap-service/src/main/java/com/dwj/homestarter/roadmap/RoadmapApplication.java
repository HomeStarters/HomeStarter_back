package com.dwj.homestarter.roadmap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Roadmap Service Application
 * 장기주거 로드맵 서비스의 메인 애플리케이션 클래스
 */
@SpringBootApplication(scanBasePackages = {
    "com.dwj.homestarter.roadmap",
    "com.dwj.homestarter.common"
})
@EnableJpaRepositories(basePackages = "com.dwj.homestarter.roadmap.repository.jpa")
@EnableFeignClients(basePackages = "com.dwj.homestarter.roadmap.client")
public class RoadmapApplication {
    public static void main(String[] args) {
        SpringApplication.run(RoadmapApplication.class, args);
    }
}
