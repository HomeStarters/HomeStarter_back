package com.dwj.homestarter.roadmap.dto.response;

import com.dwj.homestarter.roadmap.repository.entity.EventType;
import com.dwj.homestarter.roadmap.repository.entity.LifecycleEventEntity;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 생애주기 이벤트 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LifecycleEventResponse {

    /**
     * 이벤트 ID
     */
    private String id;

    /**
     * 사용자 ID
     */
    private String userId;

    /**
     * 이벤트 이름
     */
    private String name;

    /**
     * 이벤트 유형
     */
    private EventType eventType;

    /**
     * 이벤트 예정일
     */
    private String eventDate;

    /**
     * 주택 선택 고려 기준
     */
    private String housingCriteria;

    /**
     * 생성일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    private LocalDateTime updatedAt;

    /**
     * Entity를 DTO로 변환
     */
    public static LifecycleEventResponse from(LifecycleEventEntity entity) {
        return LifecycleEventResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .name(entity.getName())
                .eventType(entity.getEventType())
                .eventDate(entity.getEventDate())
                .housingCriteria(entity.getHousingCriteria())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
