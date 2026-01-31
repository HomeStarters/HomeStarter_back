package com.dwj.homestarter.roadmap.worker;

import com.dwj.homestarter.roadmap.exception.ServiceUnavailableException;
import com.dwj.homestarter.roadmap.client.AssetClient;
import com.dwj.homestarter.roadmap.client.CalculatorClient;
import com.dwj.homestarter.roadmap.client.HousingClient;
import com.dwj.homestarter.roadmap.client.LlmClient;
import com.dwj.homestarter.roadmap.client.dto.*;
import com.dwj.homestarter.roadmap.repository.entity.*;
import com.dwj.homestarter.roadmap.repository.jpa.*;
import com.dwj.homestarter.roadmap.service.sse.SseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 로드맵 생성 워커
 * Kafka 메시지를 수신하여 로드맵을 생성하는 비동기 작업자
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoadmapWorker {

    private static final String ROADMAP_CACHE_KEY_PREFIX = "roadmap:";
    private static final String ROADMAP_GENERATION_TOPIC = "roadmap.generation";

    @Value("${llm.api-key:}")
    private String llmApiKey;

    private final RoadmapRepository roadmapRepository;
    private final RoadmapStageRepository roadmapStageRepository;
    private final ExecutionGuideRepository executionGuideRepository;
    private final RoadmapTaskRepository roadmapTaskRepository;
    private final LifecycleEventRepository lifecycleEventRepository;
    private final AssetClient assetClient;
    private final HousingClient housingClient;
    private final CalculatorClient calculatorClient;
    private final LlmClient llmClient;
    private final SseService sseService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 로드맵 생성 메시지 수신 및 처리
     * Kafka Consumer로 로드맵 생성 요청을 비동기로 처리
     *
     * @param message 메시지 내용 (taskId, userId, authorization)
     * @param topic Kafka 토픽 이름
     * @param acknowledgment Kafka acknowledgment
     */
    @KafkaListener(
        topics = "${kafka.topics.roadmap-generation:roadmap.generation}",
        groupId = "roadmap-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void generateRoadmap(
            @Payload Map<String, Object> message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        String taskId = (String) message.get("taskId");
        String userId = (String) message.get("userId");
        String authorization = (String) message.get("authorization");

        log.info("Processing roadmap generation from Kafka: topic={}, taskId={}, userId={}", topic, taskId, userId);

        try {
            // 1. 작업 상태 업데이트 (PROCESSING)
            updateTaskStatus(taskId, TaskStatus.PROCESSING, 0, "로드맵 생성을 시작합니다");
            sendProgress(taskId, 0, "로드맵 생성을 시작합니다", "PROCESSING");

            // 2. 외부 서비스에서 데이터 수집
            updateTaskStatus(taskId, TaskStatus.PROCESSING, 20, "필요한 정보를 수집하고 있습니다");
            sendProgress(taskId, 20, "필요한 정보를 수집하고 있습니다", "PROCESSING");

            Map<String, Object> collectedData = collectData(userId, authorization);

            // 3. AI 로드맵 생성 요청
            updateTaskStatus(taskId, TaskStatus.PROCESSING, 40, "AI가 로드맵을 생성하고 있습니다");
            sendProgress(taskId, 40, "AI가 로드맵을 생성하고 있습니다", "PROCESSING");

            LlmResponse llmResponse = requestLlmRoadmap(collectedData);

            // 4. 로드맵 저장
            updateTaskStatus(taskId, TaskStatus.PROCESSING, 80, "로드맵을 저장하고 있습니다");
            sendProgress(taskId, 80, "로드맵을 저장하고 있습니다", "PROCESSING");

            String roadmapId = saveRoadmap(taskId, userId, llmResponse, collectedData);

            // 5. 작업 완료
            updateTaskStatus(taskId, TaskStatus.COMPLETED, 100, "로드맵 생성이 완료되었습니다", roadmapId);
            sendComplete(taskId, roadmapId);

            // 6. 캐시 무효화
            invalidateRoadmapCache(userId);

            log.info("Roadmap generation completed: taskId={}, roadmapId={}", taskId, roadmapId);

            // 7. Kafka 메시지 처리 완료 확인
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Roadmap generation failed: taskId={}", taskId, e);

            String errorMessage = "로드맵 생성에 실패했습니다: " + e.getMessage();
            updateTaskStatus(taskId, TaskStatus.FAILED, 0, errorMessage, null, errorMessage);
            sendError(taskId, errorMessage);

            // 재시도를 위해 acknowledge하지 않음
            throw e;
        }
    }

    /**
     * 외부 서비스에서 데이터 수집
     */
    private Map<String, Object> collectData(String userId, String authorization) {
        Map<String, Object> data = new HashMap<>();

        try {
            // 자산 정보 조회
            AssetSummaryDto assets = assetClient.getAssetSummary(authorization);
            data.put("assets", assets);

            // 최종목표 주택 조회
            HousingInfoDto finalHousing = housingClient.getFinalGoalHousing(authorization);
            data.put("finalHousing", finalHousing);

            // 생애주기 이벤트 조회
            List<LifecycleEventEntity> events = lifecycleEventRepository.findByUserIdOrderByEventDateAsc(userId);
            data.put("lifecycleEvents", events);

            // 재무 계산 결과 조회 (optional)
            try {
                CalculationResultDto calculation = calculatorClient.getCalculationResult(authorization);
                data.put("calculation", calculation);
            } catch (Exception e) {
                log.warn("Failed to get calculation result: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("Failed to collect data from external services", e);
            throw new ServiceUnavailableException("외부 서비스 데이터 수집 실패: " + e.getMessage());
        }

        return data;
    }

    /**
     * LLM에 로드맵 생성 요청
     */
    private LlmResponse requestLlmRoadmap(Map<String, Object> collectedData) {
        try {
            // LLM 요청 생성
            LlmRequest llmRequest = buildLlmRequest(collectedData);

            // LLM 호출 (환경변수에서 API Key 사용)
            String apiKey = llmApiKey.startsWith("Bearer ") ? llmApiKey : "Bearer " + llmApiKey;
            LlmResponse response = llmClient.generateRoadmap(apiKey, llmRequest);

            if (response == null || response.getGeneratedText() == null) {
                throw new ServiceUnavailableException("LLM 응답이 비어있습니다");
            }

            return response;

        } catch (Exception e) {
            log.error("Failed to request LLM roadmap generation", e);
            throw new ServiceUnavailableException("AI 로드맵 생성 실패: " + e.getMessage());
        }
    }

    /**
     * LLM 요청 생성
     */
    private LlmRequest buildLlmRequest(Map<String, Object> collectedData) {
        // LLM에 전달할 프롬프트 생성
        StringBuilder prompt = new StringBuilder();
        prompt.append("사용자의 장기주거 로드맵을 생성해주세요.\n\n");

        // 자산 정보
        AssetSummaryDto assets = (AssetSummaryDto) collectedData.get("assets");
        if (assets != null) {
            prompt.append("## 현재 자산\n");
            long totalAssets = (assets.getMyTotalAssets() != null ? assets.getMyTotalAssets() : 0L) +
                               (assets.getSpouseTotalAssets() != null ? assets.getSpouseTotalAssets() : 0L);
            long totalIncome = (assets.getMyMonthlyIncome() != null ? assets.getMyMonthlyIncome() : 0L) +
                               (assets.getSpouseMonthlyIncome() != null ? assets.getSpouseMonthlyIncome() : 0L);
            prompt.append(String.format("- 총 자산: %,d원\n", totalAssets));
            prompt.append(String.format("- 월 총 수입: %,d원\n", totalIncome));
            prompt.append(String.format("- 월 지출: %,d원\n\n", assets.getMonthlyExpense() != null ? assets.getMonthlyExpense() : 0L));
        }

        // 최종목표 주택
        HousingInfoDto finalHousing = (HousingInfoDto) collectedData.get("finalHousing");
        if (finalHousing != null) {
            prompt.append("## 최종목표 주택\n");
            prompt.append(String.format("- 주택명: %s\n", finalHousing.getName()));
            prompt.append(String.format("- 가격: %,d원\n", finalHousing.getPrice()));
            prompt.append(String.format("- 목표 입주일: %s\n\n", finalHousing.getMoveInDate()));
        }

        // 생애주기 이벤트
        @SuppressWarnings("unchecked")
        List<LifecycleEventEntity> events = (List<LifecycleEventEntity>) collectedData.get("lifecycleEvents");
        if (events != null && !events.isEmpty()) {
            prompt.append("## 생애주기 이벤트\n");
            for (LifecycleEventEntity event : events) {
                prompt.append(String.format("- %s (%s, %s)\n",
                        event.getName(), event.getEventType(), event.getEventDate()));
            }
            prompt.append("\n");
        }

        prompt.append("위 정보를 바탕으로 단계별 장기주거 로드맵을 생성해주세요. JSON 형식으로 응답해주세요.");

        return LlmRequest.builder()
                .prompt(prompt.toString())
                .maxTokens(4000)
                .temperature(0.7)
                .build();
    }

    /**
     * 로드맵 저장
     */
    private String saveRoadmap(String taskId, String userId, LlmResponse llmResponse,
                               Map<String, Object> collectedData) {

        // 버전 계산
        int version = roadmapRepository.findMaxVersionByUserId(userId).orElse(0) + 1;

        // 최종목표 주택 정보
        HousingInfoDto finalHousing = (HousingInfoDto) collectedData.get("finalHousing");

        // 로드맵 엔티티 생성
        RoadmapEntity roadmap = RoadmapEntity.builder()
                .userId(userId)
                .taskId(taskId)
                .version(version)
                .status(RoadmapStatus.COMPLETED)
                .finalHousingId(finalHousing.getId())
                .build();

        RoadmapEntity savedRoadmap = roadmapRepository.save(roadmap);

        try {
            // LLM 응답 파싱 및 단계별 계획 저장
            saveRoadmapStages(savedRoadmap.getId(), llmResponse);

            // 실행 가이드 저장
            saveExecutionGuide(savedRoadmap.getId(), llmResponse);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize data for roadmap", e);
            throw new RuntimeException("로드맵 데이터 저장 중 오류 발생: " + e.getMessage(), e);
        }

        return savedRoadmap.getId();
    }

    /**
     * 로드맵 단계 저장
     */
    private void saveRoadmapStages(String roadmapId, LlmResponse llmResponse) throws JsonProcessingException {
        // 실제 구현에서는 LLM 응답을 파싱하여 단계별 정보 추출
        // 여기서는 예시로 간단한 더미 데이터 생성

        List<String> dummyFeatures = List.of("출퇴근 편리", "신혼집 적합");
        List<String> dummyTips = List.of("월 저축 습관 형성이 중요", "전세 보증금 마련");

        RoadmapStageEntity stage = RoadmapStageEntity.builder()
                .roadmapId(roadmapId)
                .stageNumber(1)
                .stageName("신혼집")
                .moveInDate("2025-06")
                .duration(36)
                .estimatedPrice(600000000L)
                .location("직장 근처 역세권")
                .type("전용 59㎡")
                .features(objectMapper.writeValueAsString(dummyFeatures))
                .targetSavings(100000000L)
                .monthlySavings(2000000L)
                .loanAmount(400000000L)
                .loanProduct("신혼부부 특별대출")
                .strategy("전세로 시작하여 자금 마련")
                .tips(objectMapper.writeValueAsString(dummyTips))
                .build();

        roadmapStageRepository.save(stage);
    }

    /**
     * 실행 가이드 저장
     */
    private void saveExecutionGuide(String roadmapId, LlmResponse llmResponse) throws JsonProcessingException {
        // 실제 구현에서는 LLM 응답을 파싱하여 가이드 정보 추출
        // 여기서는 예시로 간단한 더미 데이터 생성

        List<Map<String, Object>> monthlySavings = new ArrayList<>();
        Map<String, Object> plan = new HashMap<>();
        plan.put("period", "2025-06 ~ 2028-05");
        plan.put("amount", 2000000);
        plan.put("purpose", "1단계 → 2단계 전환 자금");
        monthlySavings.add(plan);

        List<String> warnings = List.of("금리 변동에 따른 대출상환액 증가 가능성");
        List<String> tips = List.of("1단계에서 월 저축 습관 형성이 중요");

        ExecutionGuideEntity guide = ExecutionGuideEntity.builder()
                .roadmapId(roadmapId)
                .monthlySavingsPlan(objectMapper.writeValueAsString(monthlySavings))
                .warnings(objectMapper.writeValueAsString(warnings))
                .tips(objectMapper.writeValueAsString(tips))
                .build();

        executionGuideRepository.save(guide);
    }

    /**
     * 작업 상태 업데이트
     */
    private void updateTaskStatus(String taskId, TaskStatus status, int progress, String message) {
        updateTaskStatus(taskId, status, progress, message, null, null);
    }

    /**
     * 작업 상태 업데이트 (roadmapId 포함)
     */
    private void updateTaskStatus(String taskId, TaskStatus status, int progress, String message,
                                   String roadmapId) {
        updateTaskStatus(taskId, status, progress, message, roadmapId, null);
    }

    /**
     * 작업 상태 업데이트 (전체)
     */
    private void updateTaskStatus(String taskId, TaskStatus status, int progress, String message,
                                   String roadmapId, String errorMessage) {
        RoadmapTaskEntity task = roadmapTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalStateException("작업을 찾을 수 없습니다: " + taskId));

        task.setStatus(status);
        task.setProgress(progress);
        task.setMessage(message);

        if (roadmapId != null) {
            task.setRoadmapId(roadmapId);
        }

        if (errorMessage != null) {
            task.setErrorMessage(errorMessage);
        }

        roadmapTaskRepository.save(task);
    }

    /**
     * SSE 진행 상황 전송
     */
    private void sendProgress(String taskId, int progress, String message, String status) {
        if (sseService.hasEmitter(taskId)) {
            sseService.sendProgress(taskId, progress, message, status);
        }
    }

    /**
     * SSE 완료 이벤트 전송
     */
    private void sendComplete(String taskId, String roadmapId) {
        if (sseService.hasEmitter(taskId)) {
            sseService.sendComplete(taskId, roadmapId);
        }
    }

    /**
     * SSE 실패 이벤트 전송
     */
    private void sendError(String taskId, String error) {
        if (sseService.hasEmitter(taskId)) {
            sseService.sendError(taskId, error);
        }
    }

    /**
     * 로드맵 캐시 무효화
     */
    private void invalidateRoadmapCache(String userId) {
        String cacheKey = ROADMAP_CACHE_KEY_PREFIX + userId;
        redisTemplate.delete(cacheKey);
        log.debug("Invalidated roadmap cache for user: {}", userId);
    }
}
