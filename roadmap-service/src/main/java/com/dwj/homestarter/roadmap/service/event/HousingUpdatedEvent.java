package com.dwj.homestarter.roadmap.service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Housing 변경 이벤트
 * Housing 서비스에서 주택 정보가 변경될 때 발행되는 이벤트
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HousingUpdatedEvent {

    /**
     * 이벤트 ID
     */
    private String eventId;

    /**
     * 주택 ID
     */
    private String housingId;

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
