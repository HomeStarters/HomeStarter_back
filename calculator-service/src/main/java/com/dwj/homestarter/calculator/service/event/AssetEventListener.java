package com.dwj.homestarter.calculator.service.event;

import com.dwj.homestarter.calculator.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Asset 이벤트 리스너
 * Asset 변경 이벤트를 수신하여 캐시를 무효화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssetEventListener {

    private final CacheService cacheService;

    /**
     * Asset 업데이트 이벤트 처리
     * Asset 서비스에서 자산 정보가 변경되면 해당 사용자의 계산 결과 캐시를 무효화
     *
     * @param payload 이벤트 데이터
     * @param acknowledgment Kafka acknowledgment
     */
    @KafkaListener(
        topics = "asset.updated",
        groupId = "calculator-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAssetUpdated(
            @Payload Map<String, Object> payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {

        try {
            log.info("Asset 업데이트 이벤트 수신 - Topic: {}, Payload: {}", topic, payload);

            // userId 추출
            String userId = (String) payload.get("userId");
            if (userId == null || userId.isEmpty()) {
                log.warn("userId가 없는 Asset 이벤트: {}", payload);
                acknowledgment.acknowledge();
                return;
            }

            // 사용자의 모든 계산 결과 캐시 무효화
            invalidateCacheByUserId(userId);

            log.info("Asset 업데이트 이벤트 처리 완료 - userId: {}", userId);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Asset 이벤트 처리 중 오류 발생", e);
            // 재시도를 위해 acknowledge하지 않음
            throw e;
        }
    }

    /**
     * 사용자 ID로 캐시 무효화
     * 해당 사용자의 모든 계산 결과 캐시 삭제
     *
     * @param userId 사용자 ID
     */
    private void invalidateCacheByUserId(String userId) {
        try {
            // 계산 결과 캐시 삭제 (calc:{userId}:*)
            String calcPattern = "calc:" + userId + ":*";
            Long deletedCalc = cacheService.deletePattern(calcPattern);
            log.debug("계산 결과 캐시 삭제 - Pattern: {}, Count: {}", calcPattern, deletedCalc);

            // 목록 캐시 삭제 (calc:list:{userId}:*)
            String listPattern = "calc:list:" + userId + ":*";
            Long deletedList = cacheService.deletePattern(listPattern);
            log.debug("목록 캐시 삭제 - Pattern: {}, Count: {}", listPattern, deletedList);

            log.info("사용자 캐시 무효화 완료 - userId: {}, 삭제된 캐시: {} 개",
                    userId, (deletedCalc + deletedList));

        } catch (Exception e) {
            log.error("캐시 무효화 중 오류 발생 - userId: {}", userId, e);
            throw e;
        }
    }
}
