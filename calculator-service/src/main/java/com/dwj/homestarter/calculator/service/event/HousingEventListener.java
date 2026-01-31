package com.dwj.homestarter.calculator.service.event;

import com.dwj.homestarter.calculator.repository.jpa.CalculatorRepository;
import com.dwj.homestarter.calculator.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Housing 이벤트 리스너
 * Housing 변경 이벤트를 수신하여 캐시를 무효화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HousingEventListener {

    private final CacheService cacheService;
    private final CalculatorRepository calculatorRepository;

    /**
     * Housing 업데이트 이벤트 처리
     * Housing 서비스에서 주택 정보가 변경되면 해당 주택과 관련된 모든 계산 결과 캐시를 무효화
     *
     * @param payload 이벤트 데이터
     * @param acknowledgment Kafka acknowledgment
     */
    @KafkaListener(
        topics = "housing.updated",
        groupId = "calculator-service",
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

            // 주택 관련 모든 계산 결과 캐시 무효화
            invalidateCacheByHousingId(housingId);

            log.info("Housing 업데이트 이벤트 처리 완료 - housingId: {}", housingId);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Housing 이벤트 처리 중 오류 발생", e);
            // 재시도를 위해 acknowledge하지 않음
            throw e;
        }
    }

    /**
     * 주택 ID로 캐시 무효화
     * 해당 주택과 관련된 모든 계산 결과 캐시 삭제
     *
     * @param housingId 주택 ID
     */
    private void invalidateCacheByHousingId(String housingId) {
        try {
            // 1. 해당 주택과 관련된 사용자 ID 목록 조회
            List<String> affectedUserIds = calculatorRepository.findUserIdsByHousingId(housingId);
            log.debug("영향받는 사용자 수: {} - housingId: {}", affectedUserIds.size(), housingId);

            long totalDeleted = 0;

            // 2. 각 사용자의 캐시 무효화
            for (String userId : affectedUserIds) {
                // 계산 결과 캐시 삭제 (calc:{userId}:{housingId}:*)
                String calcPattern = "calc:" + userId + ":" + housingId + ":*";
                Long deletedCalc = cacheService.deletePattern(calcPattern);
                log.debug("계산 결과 캐시 삭제 - Pattern: {}, Count: {}", calcPattern, deletedCalc);
                totalDeleted += (deletedCalc != null ? deletedCalc : 0);

                // 목록 캐시 삭제 (calc:list:{userId}:*)
                String listPattern = "calc:list:" + userId + ":*";
                Long deletedList = cacheService.deletePattern(listPattern);
                log.debug("목록 캐시 삭제 - Pattern: {}, Count: {}", listPattern, deletedList);
                totalDeleted += (deletedList != null ? deletedList : 0);
            }

            log.info("주택 캐시 무효화 완료 - housingId: {}, 영향받는 사용자: {} 명, 삭제된 캐시: {} 개",
                    housingId, affectedUserIds.size(), totalDeleted);

        } catch (Exception e) {
            log.error("캐시 무효화 중 오류 발생 - housingId: {}", housingId, e);
            throw e;
        }
    }
}
