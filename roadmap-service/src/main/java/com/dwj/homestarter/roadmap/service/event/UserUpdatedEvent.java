package com.dwj.homestarter.roadmap.service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User 변경 이벤트
 * User 서비스에서 사용자 정보가 변경될 때 발행되는 이벤트
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {

    /**
     * 이벤트 ID
     */
    private String eventId;

    /**
     * 사용자 ID
     */
    private String userId;

    /**
     * 변경된 필드
     */
    private String updatedField;

    /**
     * 이벤트 발생 시각
     */
    private LocalDateTime occurredAt;

    /**
     * 이벤트 타입
     */
    private String eventType;
}
