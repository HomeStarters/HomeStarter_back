package com.dwj.homestarter.calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Calculator Service 메인 애플리케이션
 *
 * 대출 및 주택 계산 서비스
 *
 * @author homestarter
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {
        "com.dwj.homestarter.calculator",
        "com.dwj.homestarter.common"
})
@EnableJpaRepositories(basePackages = "com.dwj.homestarter.calculator.repository")
public class CalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CalculatorApplication.class, args);
    }
}
