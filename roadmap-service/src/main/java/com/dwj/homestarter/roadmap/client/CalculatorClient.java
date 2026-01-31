package com.dwj.homestarter.roadmap.client;

import com.dwj.homestarter.roadmap.client.dto.CalculationResultDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Calculator Service Feign Client
 * 입주 후 지출 계산 결과 조회
 */
@FeignClient(
    name = "calculator-service",
    url = "${feign.client.url.calculator-service:http://localhost:8085}"
)
public interface CalculatorClient {

    /**
     * 계산 결과 조회
     *
     * @param authorization JWT 토큰
     * @return 계산 결과
     */
    @GetMapping("/calculations/result")
    CalculationResultDto getCalculationResult(@RequestHeader("Authorization") String authorization);
}
