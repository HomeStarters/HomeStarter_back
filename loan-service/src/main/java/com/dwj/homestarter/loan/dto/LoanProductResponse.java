package com.dwj.homestarter.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대출상품 단일 응답 DTO
 *
 * 단일 대출상품 조회/생성/수정 응답 객체
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProductResponse {

    /**
     * 성공 여부
     */
    private boolean success;

    /**
     * 대출상품 데이터
     */
    private LoanProductDTO data;

    /**
     * 응답 메시지
     */
    private String message;

    /**
     * 성공 응답 생성
     *
     * @param data 대출상품 DTO
     * @param message 메시지
     * @return LoanProductResponse
     */
    public static LoanProductResponse success(LoanProductDTO data, String message) {
        return LoanProductResponse.builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }
}
