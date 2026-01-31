package com.dwj.homestarter.roadmap.service.impl;

import com.dwj.homestarter.common.exception.NotFoundException;
import com.dwj.homestarter.common.exception.ValidationException;
import com.dwj.homestarter.roadmap.client.HousingClient;
import com.dwj.homestarter.roadmap.client.dto.HousingInfoDto;
import com.dwj.homestarter.roadmap.dto.response.*;
import com.dwj.homestarter.roadmap.exception.ConflictException;
import com.dwj.homestarter.roadmap.repository.entity.*;
import com.dwj.homestarter.roadmap.repository.jpa.*;
import com.dwj.homestarter.roadmap.service.LifecycleEventService;
import com.dwj.homestarter.roadmap.service.RoadmapService;
import com.dwj.homestarter.roadmap.service.message.MessagePublisher;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 로드맵 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoadmapServiceImpl implements RoadmapService {

    private static final String ROADMAP_CACHE_KEY_PREFIX = "roadmap:";
    private static final long CACHE_TTL_MINUTES = 30;
    private static final int ESTIMATED_COMPLETION_TIME_SECONDS = 30;

    private final RoadmapRepository roadmapRepository;
    private final RoadmapStageRepository roadmapStageRepository;
    private final ExecutionGuideRepository executionGuideRepository;
    private final RoadmapTaskRepository roadmapTaskRepository;
    private final LifecycleEventService lifecycleEventService;
    private final HousingClient housingClient;
    private final MessagePublisher messagePublisher;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RoadmapTaskResponse generateRoadmap(String userId, String authorization) {
        log.info("Generating roadmap for user: {}", userId);

        // 1. 필수 조건 검증
        validateRoadmapPrerequisites(userId, authorization);

        // 2. 진행 중인 작업 확인
        checkOngoingTask(userId);

        // 3. 작업 생성
        RoadmapTaskEntity task = RoadmapTaskEntity.builder()
                .userId(userId)
                .status(TaskStatus.PENDING)
                .progress(0)
                .message("로드맵 생성 요청을 처리 중입니다")
                .build();

        RoadmapTaskEntity savedTask = roadmapTaskRepository.save(task);

        // 4. 메시지 발행 (비동기 처리 시작)
        messagePublisher.publishRoadmapGenerationMessage(savedTask.getId(), userId, authorization);

        log.info("Roadmap generation task created: {}", savedTask.getId());

        return RoadmapTaskResponse.builder()
                .requestId(savedTask.getId())
                .status(TaskStatus.PROCESSING)
                .progress(0)
                .message("로드맵을 생성하고 있습니다")
                .estimatedCompletionTime(ESTIMATED_COMPLETION_TIME_SECONDS)
                .build();
    }

    @Override
    public RoadmapResponse getRoadmap(String userId, Integer version) {
        log.info("Getting roadmap for user: {}, version: {}", userId, version);

        // 캐시 조회 (최신 버전인 경우만)
        if (version == null) {
            String cacheKey = ROADMAP_CACHE_KEY_PREFIX + userId;
            RoadmapResponse cached = (RoadmapResponse) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Roadmap cache hit for user: {}", userId);
                return cached;
            }
        }

        // DB 조회
        RoadmapEntity roadmap;
        if (version == null) {
            roadmap = roadmapRepository.findTopByUserIdAndStatusOrderByVersionDesc(userId, RoadmapStatus.COMPLETED)
                    .orElseThrow(() -> new NotFoundException("생성된 로드맵이 없습니다"));
        } else {
            roadmap = roadmapRepository.findByUserIdAndVersion(userId, version)
                    .orElseThrow(() -> new NotFoundException("로드맵 버전을 찾을 수 없습니다"));
        }

        // 로드맵 상세 정보 조회
        RoadmapResponse response = buildRoadmapResponse(roadmap);

        // 캐시 저장 (최신 버전인 경우만)
        if (version == null) {
            String cacheKey = ROADMAP_CACHE_KEY_PREFIX + userId;
            redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("Roadmap cached for user: {}", userId);
        }

        return response;
    }

    @Override
    @Transactional
    public RoadmapTaskResponse regenerateRoadmap(String userId, String authorization) {
        log.info("Regenerating roadmap for user: {}", userId);

        // 기존 로드맵 확인
        if (!roadmapRepository.existsByUserId(userId)) {
            throw new NotFoundException("기존 로드맵이 없습니다");
        }

        // 나머지는 생성과 동일
        return generateRoadmap(userId, authorization);
    }

    @Override
    public RoadmapStatusResponse getRoadmapStatus(String userId, String requestId) {
        log.info("Getting roadmap status: {} for user: {}", requestId, userId);

        RoadmapTaskEntity task = roadmapTaskRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("작업을 찾을 수 없습니다"));

        // 권한 확인
        if (!task.getUserId().equals(userId)) {
            throw new NotFoundException("작업을 찾을 수 없습니다");
        }

        RoadmapStatusResponse.RoadmapStatusResponseBuilder builder = RoadmapStatusResponse.builder()
                .requestId(task.getId())
                .status(task.getStatus())
                .progress(task.getProgress())
                .message(task.getMessage());

        // 완료된 경우 로드맵 ID 포함
        if (task.getStatus() == TaskStatus.COMPLETED && task.getRoadmapId() != null) {
            builder.roadmapId(task.getRoadmapId());
        }

        // 실패한 경우 에러 메시지 포함
        if (task.getStatus() == TaskStatus.FAILED && task.getErrorMessage() != null) {
            builder.error(task.getErrorMessage());
        }

        return builder.build();
    }

    @Override
    public RoadmapVersionListResponse getRoadmapVersions(String userId) {
        log.info("Getting roadmap versions for user: {}", userId);

        List<RoadmapEntity> roadmaps = roadmapRepository.findTop3ByUserIdOrderByVersionDesc(userId);

        List<RoadmapVersionInfo> versions = roadmaps.stream()
                .map(r -> RoadmapVersionInfo.builder()
                        .version(r.getVersion())
                        .createdAt(r.getCreatedAt())
                        .changeDescription(generateChangeDescription(r))
                        .build())
                .collect(Collectors.toList());

        return RoadmapVersionListResponse.builder()
                .versions(versions)
                .build();
    }

    /**
     * 로드맵 생성 필수 조건 검증
     */
    private void validateRoadmapPrerequisites(String userId, String authorization) {
        // 최종목표 주택 확인
        try {
            HousingInfoDto finalGoalHousing = housingClient.getFinalGoalHousing(authorization);
            if (finalGoalHousing == null) {
                throw new ValidationException("최종목표 주택이 설정되지 않았습니다");
            }
        } catch (Exception e) {
            log.error("Failed to get final goal housing: {}", e.getMessage());
            throw new ValidationException("최종목표 주택 조회 실패: " + e.getMessage());
        }

        // 생애주기 이벤트 확인
        long eventCount = lifecycleEventService.countByUserId(userId);
        if (eventCount == 0) {
            throw new ValidationException("생애주기 이벤트가 등록되지 않았습니다");
        }
    }

    /**
     * 진행 중인 작업 확인
     */
    private void checkOngoingTask(String userId) {
        List<RoadmapTaskEntity> ongoingTasks = roadmapTaskRepository.findByUserIdAndStatusIn(
                userId,
                List.of(TaskStatus.PENDING, TaskStatus.PROCESSING)
        );

        if (!ongoingTasks.isEmpty()) {
            throw new ConflictException("이미 진행 중인 로드맵 생성 작업이 있습니다");
        }
    }

    /**
     * 로드맵 응답 생성
     */
    private RoadmapResponse buildRoadmapResponse(RoadmapEntity roadmap) {
        // 단계별 계획 조회
        List<RoadmapStageEntity> stages = roadmapStageRepository.findByRoadmapIdOrderByStageNumberAsc(roadmap.getId());

        // 실행 가이드 조회
        ExecutionGuideEntity guideEntity = executionGuideRepository.findByRoadmapId(roadmap.getId())
                .orElse(null);

        return RoadmapResponse.builder()
                .id(roadmap.getId())
                .userId(roadmap.getUserId())
                .version(roadmap.getVersion())
                .status(roadmap.getStatus())
                .goalHousing(buildGoalHousingDto(roadmap))
                .stages(buildRoadmapStageDtos(stages))
                .executionGuide(buildExecutionGuideDto(guideEntity))
                .createdAt(roadmap.getCreatedAt())
                .updatedAt(roadmap.getUpdatedAt())
                .build();
    }

    private GoalHousingDto buildGoalHousingDto(RoadmapEntity roadmap) {
        // 실제 구현에서는 Housing Service에서 정보를 가져와야 함
        return GoalHousingDto.builder()
                .id(roadmap.getFinalHousingId())
                .build();
    }

    private List<RoadmapStageDto> buildRoadmapStageDtos(List<RoadmapStageEntity> stages) {
        return stages.stream()
                .map(this::convertToStageDto)
                .collect(Collectors.toList());
    }

    private RoadmapStageDto convertToStageDto(RoadmapStageEntity entity) {
        return RoadmapStageDto.builder()
                .stageNumber(entity.getStageNumber())
                .stageName(entity.getStageName())
                .moveInDate(entity.getMoveInDate())
                .duration(entity.getDuration())
                .housingCharacteristics(HousingCharacteristicsDto.builder()
                        .estimatedPrice(entity.getEstimatedPrice())
                        .location(entity.getLocation())
                        .type(entity.getType())
                        .features(parseJsonList(entity.getFeatures()))
                        .build())
                .financialGoals(FinancialGoalsDto.builder()
                        .targetSavings(entity.getTargetSavings())
                        .monthlySavings(entity.getMonthlySavings())
                        .loanAmount(entity.getLoanAmount())
                        .loanProduct(entity.getLoanProduct())
                        .build())
                .strategy(entity.getStrategy())
                .tips(parseJsonList(entity.getTips()))
                .build();
    }

    private ExecutionGuideDto buildExecutionGuideDto(ExecutionGuideEntity entity) {
        if (entity == null) {
            return null;
        }

        return ExecutionGuideDto.builder()
                .monthlySavingsPlan(parseJsonToMonthlySavingsPlan(entity.getMonthlySavingsPlan()))
                .warnings(parseJsonList(entity.getWarnings()))
                .tips(parseJsonList(entity.getTips()))
                .build();
    }

    private List<String> parseJsonList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Failed to parse JSON list: {}", e.getMessage());
            return List.of();
        }
    }

    private List<MonthlySavingsPlanDto> parseJsonToMonthlySavingsPlan(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<MonthlySavingsPlanDto>>() {});
        } catch (Exception e) {
            log.error("Failed to parse monthly savings plan: {}", e.getMessage());
            return List.of();
        }
    }

    private String generateChangeDescription(RoadmapEntity roadmap) {
        if (roadmap.getVersion() == 1) {
            return "최초 생성";
        }
        return "로드맵 재설계";
    }
}
