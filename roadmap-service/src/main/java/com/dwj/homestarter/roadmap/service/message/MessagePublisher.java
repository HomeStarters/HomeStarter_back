package com.dwj.homestarter.roadmap.service.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 메시지 발행자
 * 로드맵 생성 작업 메시지를 발행
 *
 * 참고: Roadmap 서비스는 비동기 작업 처리를 위해 메시지 큐를 사용합니다.
 * RabbitMQ에서 Kafka로 마이그레이션되었으나, Worker는 아직 RabbitListener를 사용합니다.
 * 향후 Kafka Consumer로 변경 예정입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessagePublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ROADMAP_GENERATION_TOPIC = "roadmap.generation";

    /**
     * 로드맵 생성 메시지 발행
     *
     * @param taskId 작업 ID
     * @param userId 사용자 ID
     * @param authorization JWT 토큰
     */
    public void publishRoadmapGenerationMessage(String taskId, String userId, String authorization) {
        log.info("Publishing roadmap generation message: taskId={}, userId={}", taskId, userId);

        Map<String, Object> message = new HashMap<>();
        message.put("taskId", taskId);
        message.put("userId", userId);
        message.put("authorization", authorization);
        message.put("timestamp", System.currentTimeMillis());

        kafkaTemplate.send(ROADMAP_GENERATION_TOPIC, userId, message);

        log.debug("Roadmap generation message published successfully to topic: {}", ROADMAP_GENERATION_TOPIC);
    }
}
