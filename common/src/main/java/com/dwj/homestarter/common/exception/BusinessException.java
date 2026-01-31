package com.dwj.homestarter.common.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 예외
 *
 * 비즈니스 규칙 위반 시 발생하는 예외 클래스
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 에러 코드
     */
    private final String errorCode;

    /**
     * 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
