package com.dwj.homestarter.asset.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Calculator Service REST 클라이언트
 * calculator-service의 월 상환액 계산 API를 호출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CalculatorServiceClient {

    private final WebClient calculatorServiceWebClient;

    /**
     * 대출 월 상환액 계산
     * calculator-service의 POST /calculator/monthly-payment API 호출
     *
     * @param token              JWT 토큰 (Bearer 포함)
     * @param loanType           대출 유형
     * @param repaymentType      상환 유형
     * @param loanAmount         대출 원금
     * @param annualInterestRate 연 이자율 (%)
     * @param repaymentPeriod    상환기간 (개월)
     * @param gracePeriod        거치기간 (개월)
     * @return 월 상환액 (계산 실패 시 null)
     */
    public Long calculateMonthlyPayment(String token, String loanType, String repaymentType,
                                         Long loanAmount, Double annualInterestRate,
                                         Integer repaymentPeriod, Integer gracePeriod) {
        try {
            log.info("calculator-service 월 상환액 계산 요청 - loanAmount: {}, interestRate: {}, period: {}개월",
                    loanAmount, annualInterestRate, repaymentPeriod);

            Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("loanAmount", loanAmount);
            requestBody.put("annualInterestRate", annualInterestRate);
            requestBody.put("repaymentPeriod", repaymentPeriod);
            if (loanType != null) requestBody.put("loanType", loanType);
            if (repaymentType != null) requestBody.put("repaymentType", repaymentType);
            if (gracePeriod != null) requestBody.put("gracePeriod", gracePeriod);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = calculatorServiceWebClient.post()
                    .uri("/calculator/monthly-payment")
                    .header("Authorization", token)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                log.warn("calculator-service 응답 없음");
                return null;
            }

            Object monthlyPayment = response.get("monthlyPayment");
            if (monthlyPayment == null) {
                log.warn("calculator-service 응답에 monthlyPayment 없음");
                return null;
            }

            long result = ((Number) monthlyPayment).longValue();
            log.info("calculator-service 월 상환액 계산 완료 - monthlyPayment: {}", result);
            return result;

        } catch (Exception e) {
            log.warn("calculator-service 월 상환액 계산 중 오류 발생 - {}", e.getMessage());
            return null;
        }
    }
}
