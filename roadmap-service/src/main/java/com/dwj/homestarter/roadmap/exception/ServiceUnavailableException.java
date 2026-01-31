package com.dwj.homestarter.roadmap.exception;

import com.dwj.homestarter.common.exception.BusinessException;

/**
 * 서비스 이용 불가 예외
 *
 * 외부 서비스 장애 등으로 서비스 이용이 불가능할 때 발생하는 예외 클래스
 *
 * @author homestarter
 * @since 1.0.0
 */
public class ServiceUnavailableException extends BusinessException {

    /**
     * 생성자
     *
     * @param message 에러 메시지
     */
    public ServiceUnavailableException(String message) {
        super("SERVICE_UNAVAILABLE", message);
    }

    /**
     * 생성자
     *
     * @param message 에러 메시지
     * @param cause 원인 예외
     */
    public ServiceUnavailableException(String message, Throwable cause) {
        super("SERVICE_UNAVAILABLE", message, cause);
    }
}
