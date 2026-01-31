package com.dwj.homestarter.asset.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 자산 변경 이벤트 도메인 모델
 * Kafka를 통해 Calculator 서비스로 전달되는 이벤트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetUpdatedEvent {

    /**
     * 이벤트 ID (UUID)
     */
    private String eventId;

    /**
     * 이벤트 타입 (ASSET_UPDATED)
     */
    private String eventType;

    /**
     * 이벤트 발생 시간
     */
    private LocalDateTime timestamp;

    /**
     * 사용자 ID
     */
    private String userId;

    /**
     * 변경 유형 (CREATED, UPDATED, DELETED)
     */
    private String changeType;

    /**
     * 소유자 유형 (SELF/SPOUSE)
     */
    private OwnerType ownerType;

    /**
     * 자산 요약 정보
     */
    private AssetSummary summary;
}
