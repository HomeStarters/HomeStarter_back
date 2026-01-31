package com.dwj.homestarter.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 에러 응답 형식
 *
 * API 요청 실패 시 반환되는 에러 응답 DTO
 *
 * @author homestarter
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * 성공 여부 (항상 false)
     */
    private boolean success;

    /**
     * 에러 메시지
     */
    private String message;

    /**
     * 에러 코드
     */
    private String errorCode;

    /**
     * 에러 응답 생성
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     * @return 에러 응답 객체
     */
    public static ErrorResponse of(String errorCode, String message) {
        return new ErrorResponse(false, message, errorCode);
    }
}
