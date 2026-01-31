package com.dwj.homestarter.roadmap.repository.entity;

/**
 * 비동기 작업 상태
 *
 * PENDING: 대기 중
 * PROCESSING: 처리 중
 * COMPLETED: 완료
 * FAILED: 실패
 */
public enum TaskStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
