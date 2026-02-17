package com.dwj.homestarter.user.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 가구원 이벤트 발행자
 *
 * 가구원 변경 이벤트를 Kafka로 발행
 *
 * @author homestarter
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HouseholdEventPublisher {

    private static final String HOUSEHOLD_TOPIC = "household-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 가구원 가입 이벤트 발행
     *
     * @param householdId 가구 ID
     * @param requesterUserId 요청자 ID
     * @param targetUserId 대상자 ID
     * @param memberUserIds 전체 가구원 ID 목록
     */
    public void publishMemberJoined(String householdId, String requesterUserId, String targetUserId, List<String> memberUserIds) {
        publishEvent("MEMBER_JOINED", householdId, requesterUserId, targetUserId, memberUserIds);
    }

    /**
     * 가구원 탈퇴 이벤트 발행
     *
     * @param householdId 가구 ID
     * @param removedUserId 제거된 사용자 ID
     * @param memberUserIds 남은 가구원 ID 목록
     */
    public void publishMemberLeft(String householdId, String removedUserId, List<String> memberUserIds) {
        publishEvent("MEMBER_LEFT", householdId, removedUserId, null, memberUserIds);
    }

    private void publishEvent(String eventType, String householdId, String requesterUserId, String targetUserId, List<String> memberUserIds) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("householdId", householdId);
            event.put("requesterUserId", requesterUserId);
            if (targetUserId != null) {
                event.put("targetUserId", targetUserId);
            }
            event.put("memberUserIds", memberUserIds);
            event.put("timestamp", System.currentTimeMillis());

            String eventJson = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(HOUSEHOLD_TOPIC, householdId, eventJson)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("가구원 이벤트 발행 실패 - eventType: {}, householdId: {}", eventType, householdId, ex);
                        } else {
                            log.info("가구원 이벤트 발행 성공 - eventType: {}, householdId: {}, partition: {}",
                                    eventType, householdId, result.getRecordMetadata().partition());
                        }
                    });

        } catch (Exception e) {
            log.error("가구원 이벤트 직렬화 실패 - eventType: {}, householdId: {}", eventType, householdId, e);
        }
    }
}
