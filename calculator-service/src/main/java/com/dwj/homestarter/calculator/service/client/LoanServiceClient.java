package com.dwj.homestarter.calculator.service.client;

import com.dwj.homestarter.calculator.dto.external.wrapper.LoanProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Loan Service Feign 클라이언트
 */
@FeignClient(name = "loan-service", url = "${service.url.loan}")
public interface LoanServiceClient {

    /**
     * 대출상품 정보 조회
     *
     * @param loanProductId 대출상품 ID
     * @return LoanProductResponse 래퍼로 감싼 대출상품 정보
     */
    @GetMapping("/api/v1/loans/{loanProductId}")
    LoanProductResponse getLoanProduct(@PathVariable("loanProductId") String loanProductId);
}
