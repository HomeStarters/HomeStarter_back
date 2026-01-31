package com.dwj.homestarter.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대출상품 목록 응답 DTO
 *
 * 대출상품 목록 조회 응답 객체
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProductListResponse {

    /**
     * 성공 여부
     */
    private boolean success;

    /**
     * 대출상품 목록 데이터
     */
    private LoanProductListData data;

    /**
     * 응답 메시지
     */
    private String message;

    /**
     * 성공 응답 생성
     *
     * @param data 대출상품 목록 데이터
     * @param message 메시지
     * @return LoanProductListResponse
     */
    public static LoanProductListResponse success(LoanProductListData data, String message) {
        return LoanProductListResponse.builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }
}
