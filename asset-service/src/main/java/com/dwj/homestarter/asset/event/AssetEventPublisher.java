package com.dwj.homestarter.asset.event;

import com.dwj.homestarter.asset.domain.AssetSummary;
import com.dwj.homestarter.asset.domain.OwnerType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 자산 이벤트 발행자
 *
 * 자산정보 변경 이벤트를 Kafka로 발행
 *
 * @author homestarter
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssetEventPublisher {

    private static final String ASSET_TOPIC = "asset-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 자산정보 업데이트 이벤트 발행
     *
     * @param userId 사용자 ID
     * @param ownerType 소유자 타입
     * @param action 작업 타입 (CREATED, UPDATED, DELETED)
     * @param summary 자산 요약 정보
     */
    public void publishAssetUpdated(String userId, OwnerType ownerType, String action, AssetSummary summary) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("userId", userId);
            event.put("ownerType", ownerType.name());
            event.put("action", action);
            event.put("totalAssets", summary.getTotalAssets());
            event.put("totalLoans", summary.getTotalLoans());
            event.put("netAssets", summary.getNetAssets());
            event.put("totalMonthlyIncome", summary.getTotalMonthlyIncome());
            event.put("totalMonthlyExpense", summary.getTotalMonthlyExpense());
            event.put("monthlyAvailableFunds", summary.getMonthlyAvailableFunds());
            event.put("timestamp", System.currentTimeMillis());

            String eventJson = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(ASSET_TOPIC, userId, eventJson)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("자산 이벤트 발행 실패 - userId: {}, action: {}", userId, action, ex);
                        } else {
                            log.info("자산 이벤트 발행 성공 - userId: {}, action: {}, topic: {}, partition: {}",
                                    userId, action, ASSET_TOPIC, result.getRecordMetadata().partition());
                        }
                    });

        } catch (Exception e) {
            log.error("자산 이벤트 직렬화 실패 - userId: {}, action: {}", userId, action, e);
        }
    }
}
