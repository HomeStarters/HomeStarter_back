package com.dwj.homestarter.loan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Loan Service 메인 애플리케이션
 *
 * 대출 정보 관리 서비스
 *
 * @author homestarter
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {
        "com.dwj.homestarter.loan",
        "com.dwj.homestarter.common"
})
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.dwj.homestarter.loan.repository")
public class LoanApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanApplication.class, args);
    }
}
