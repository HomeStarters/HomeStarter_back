package com.dwj.homestarter.roadmap.service.event;

import com.dwj.homestarter.roadmap.service.CacheService;
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
 * Housing 이벤트 리스너
 * Housing 변경 이벤트를 수신하여 로드맵 캐시를 무효화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HousingEventListener {

    private final CacheService cacheService;

    /**
     * Housing 업데이트 이벤트 처리
     * Housing 서비스에서 주택 정보가 변경되면 전체 로드맵 캐시를 무효화
     * (여러 사용자가 동일한 주택을 최종목표로 설정할 수 있음)
     *
     * @param payload 이벤트 데이터
     * @param topic 토픽명
     * @param acknowledgment Kafka acknowledgment
     */
    @KafkaListener(
        topics = "housing.updated",
        groupId = "roadmap-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleHousingUpdated(
            @Payload Map<String, Object> payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {

        try {
            log.info("Housing 업데이트 이벤트 수신 - Topic: {}, Payload: {}", topic, payload);

            // housingId 추출
            String housingId = (String) payload.get("housingId");
            if (housingId == null || housingId.isEmpty()) {
                log.warn("housingId가 없는 Housing 이벤트: {}", payload);
                acknowledgment.acknowledge();
                return;
            }

            // 전체 로드맵 캐시 무효화 (간단한 전략)
            // 향후 개선: housingId로 영향받는 사용자만 무효화
            invalidateAllRoadmapCache(housingId);

            log.info("Housing 업데이트 이벤트 처리 완료 - housingId: {}", housingId);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Housing 이벤트 처리 중 오류 발생", e);
            // 재시도를 위해 acknowledge하지 않음
            throw e;
        }
    }

    /**
     * 전체 로드맵 캐시 무효화
     * 주택 정보 변경 시 영향받는 모든 로드맵 캐시 삭제
     *
     * @param housingId 주택 ID
     */
    private void invalidateAllRoadmapCache(String housingId) {
        try {
            // 전체 로드맵 캐시 삭제 (roadmap:*)
            Long deletedCount = cacheService.invalidateAllRoadmapCache();
            log.info("전체 로드맵 캐시 무효화 완료 - housingId: {}, 삭제된 캐시: {} 개",
                    housingId, deletedCount);

        } catch (Exception e) {
            log.error("캐시 무효화 중 오류 발생 - housingId: {}", housingId, e);
            throw e;
        }
    }
}
