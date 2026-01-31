package com.dwj.homestarter.common.exception;

/**
 * 인증 실패 예외
 *
 * 사용자 인증에 실패했을 때 발생하는 예외 클래스
 *
 * @author homestarter
 * @since 1.0.0
 */
public class UnauthorizedException extends BusinessException {

    /**
     * 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    public UnauthorizedException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public UnauthorizedException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
