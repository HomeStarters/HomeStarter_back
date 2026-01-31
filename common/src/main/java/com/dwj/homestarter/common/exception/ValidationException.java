package com.dwj.homestarter.common.exception;

/**
 * 입력 검증 예외
 *
 * 입력 데이터 검증에 실패했을 때 발생하는 예외 클래스
 *
 * @author homestarter
 * @since 1.0.0
 */
public class ValidationException extends BusinessException {

    /**
     * 생성자
     *
     * @param message 에러 메시지
     */
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }

    /**
     * 생성자
     *
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public ValidationException(String message, Throwable cause) {
        super("VALIDATION_ERROR", message, cause);
    }
}
