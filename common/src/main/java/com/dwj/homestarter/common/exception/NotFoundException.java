package com.dwj.homestarter.common.exception;

/**
 * 리소스 없음 예외
 *
 * 요청한 리소스를 찾을 수 없을 때 발생하는 예외 클래스
 *
 * @author homestarter
 * @since 1.0.0
 */
public class NotFoundException extends BusinessException {

    /**
     * 생성자
     *
     * @param message 에러 메시지
     */
    public NotFoundException(String message) {
        super("NOT_FOUND", message);
    }

    /**
     * 생성자
     *
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public NotFoundException(String message, Throwable cause) {
        super("NOT_FOUND", message, cause);
    }
}
