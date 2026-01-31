package com.dwj.homestarter.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 표준 API 응답 형식
 *
 * @param <T> 응답 데이터 타입
 *
 * @author homestarter
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 성공 여부
     */
    private boolean success;

    /**
     * 응답 메시지
     */
    private String message;

    /**
     * 응답 데이터
     */
    private T data;

    /**
     * 성공 응답 생성 (데이터 포함)
     *
     * @param <T> 응답 데이터 타입
     * @param message 응답 메시지
     * @param data 응답 데이터
     * @return API 응답 객체
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * 성공 응답 생성 (메시지만)
     *
     * @param message 응답 메시지
     * @return API 응답 객체
     */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /**
     * 실패 응답 생성
     *
     * @param message 응답 메시지
     * @return API 응답 객체
     */
    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
