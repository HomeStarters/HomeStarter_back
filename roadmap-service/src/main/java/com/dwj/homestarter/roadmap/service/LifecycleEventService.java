package com.dwj.homestarter.roadmap.service;

import com.dwj.homestarter.roadmap.dto.request.LifecycleEventRequest;
import com.dwj.homestarter.roadmap.dto.response.LifecycleEventListResponse;
import com.dwj.homestarter.roadmap.dto.response.LifecycleEventResponse;
import com.dwj.homestarter.roadmap.repository.entity.EventType;

/**
 * 생애주기 이벤트 서비스 인터페이스
 */
public interface LifecycleEventService {

    /**
     * 생애주기 이벤트 등록
     *
     * @param userId 사용자 ID
     * @param request 이벤트 등록 요청
     * @return 등록된 이벤트 정보
     */
    LifecycleEventResponse createLifecycleEvent(String userId, LifecycleEventRequest request);

    /**
     * 생애주기 이벤트 목록 조회
     *
     * @param userId 사용자 ID
     * @param eventType 이벤트 유형 (선택)
     * @return 이벤트 목록
     */
    LifecycleEventListResponse getLifecycleEvents(String userId, EventType eventType);

    /**
     * 생애주기 이벤트 상세 조회
     *
     * @param userId 사용자 ID
     * @param eventId 이벤트 ID
     * @return 이벤트 상세 정보
     */
    LifecycleEventResponse getLifecycleEvent(String userId, String eventId);

    /**
     * 생애주기 이벤트 수정
     *
     * @param userId 사용자 ID
     * @param eventId 이벤트 ID
     * @param request 이벤트 수정 요청
     * @return 수정된 이벤트 정보
     */
    LifecycleEventResponse updateLifecycleEvent(String userId, String eventId, LifecycleEventRequest request);

    /**
     * 생애주기 이벤트 삭제
     *
     * @param userId 사용자 ID
     * @param eventId 이벤트 ID
     */
    void deleteLifecycleEvent(String userId, String eventId);

    /**
     * 사용자의 생애주기 이벤트 개수 조회
     *
     * @param userId 사용자 ID
     * @return 이벤트 개수
     */
    long countByUserId(String userId);
}
