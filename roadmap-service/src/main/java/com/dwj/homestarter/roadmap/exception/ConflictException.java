package com.dwj.homestarter.roadmap.exception;

import com.dwj.homestarter.common.exception.BusinessException;

/**
 * 충돌 예외
 *
 * 중복 작업 요청 등 리소스 충돌 시 발생하는 예외 클래스
 *
 * @author homestarter
 * @since 1.0.0
 */
public class ConflictException extends BusinessException {

    /**
     * 생성자
     *
     * @param message 에러 메시지
     */
    public ConflictException(String message) {
        super("CONFLICT", message);
    }

    /**
     * 생성자
     *
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public ConflictException(String message, Throwable cause) {
        super("CONFLICT", message, cause);
    }
}
