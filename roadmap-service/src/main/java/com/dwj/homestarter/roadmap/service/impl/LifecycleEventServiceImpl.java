package com.dwj.homestarter.roadmap.service.impl;

import com.dwj.homestarter.common.exception.NotFoundException;
import com.dwj.homestarter.common.exception.UnauthorizedException;
import com.dwj.homestarter.roadmap.dto.request.LifecycleEventRequest;
import com.dwj.homestarter.roadmap.dto.response.LifecycleEventListResponse;
import com.dwj.homestarter.roadmap.dto.response.LifecycleEventResponse;
import com.dwj.homestarter.roadmap.repository.entity.EventType;
import com.dwj.homestarter.roadmap.repository.entity.LifecycleEventEntity;
import com.dwj.homestarter.roadmap.repository.jpa.LifecycleEventRepository;
import com.dwj.homestarter.roadmap.service.LifecycleEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 생애주기 이벤트 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LifecycleEventServiceImpl implements LifecycleEventService {

    private static final String ROADMAP_CACHE_KEY_PREFIX = "roadmap:";

    private final LifecycleEventRepository lifecycleEventRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public LifecycleEventResponse createLifecycleEvent(String userId, LifecycleEventRequest request) {
        log.info("Creating lifecycle event for user: {}, name: {}", userId, request.getName());

        LifecycleEventEntity entity = LifecycleEventEntity.builder()
                .userId(userId)
                .name(request.getName())
                .eventType(request.getEventType())
                .eventDate(request.getEventDate())
                .housingCriteria(request.getHousingCriteria())
                .build();

        LifecycleEventEntity saved = lifecycleEventRepository.save(entity);

        // 로드맵 캐시 무효화
        invalidateRoadmapCache(userId);

        return LifecycleEventResponse.from(saved);
    }

    @Override
    public LifecycleEventListResponse getLifecycleEvents(String userId, EventType eventType) {
        log.info("Getting lifecycle events for user: {}, eventType: {}", userId, eventType);

        List<LifecycleEventEntity> events;
        if (eventType != null) {
            events = lifecycleEventRepository.findByUserIdAndEventTypeOrderByEventDateAsc(userId, eventType);
        } else {
            events = lifecycleEventRepository.findByUserIdOrderByEventDateAsc(userId);
        }

        List<LifecycleEventResponse> eventResponses = events.stream()
                .map(LifecycleEventResponse::from)
                .collect(Collectors.toList());

        return LifecycleEventListResponse.builder()
                .events(eventResponses)
                .total(eventResponses.size())
                .build();
    }

    @Override
    public LifecycleEventResponse getLifecycleEvent(String userId, String eventId) {
        log.info("Getting lifecycle event: {} for user: {}", eventId, userId);

        LifecycleEventEntity entity = lifecycleEventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("생애주기 이벤트를 찾을 수 없습니다"));

        // 권한 확인
        if (!entity.getUserId().equals(userId)) {
            throw new UnauthorizedException("FORBIDDEN", "해당 이벤트에 접근할 권한이 없습니다");
        }

        return LifecycleEventResponse.from(entity);
    }

    @Override
    @Transactional
    public LifecycleEventResponse updateLifecycleEvent(String userId, String eventId, LifecycleEventRequest request) {
        log.info("Updating lifecycle event: {} for user: {}", eventId, userId);

        LifecycleEventEntity entity = lifecycleEventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("생애주기 이벤트를 찾을 수 없습니다"));

        // 권한 확인
        if (!entity.getUserId().equals(userId)) {
            throw new UnauthorizedException("FORBIDDEN", "해당 이벤트에 접근할 권한이 없습니다");
        }

        // 엔티티 업데이트
        entity.setName(request.getName());
        entity.setEventType(request.getEventType());
        entity.setEventDate(request.getEventDate());
        entity.setHousingCriteria(request.getHousingCriteria());

        LifecycleEventEntity updated = lifecycleEventRepository.save(entity);

        // 로드맵 캐시 무효화
        invalidateRoadmapCache(userId);

        return LifecycleEventResponse.from(updated);
    }

    @Override
    @Transactional
    public void deleteLifecycleEvent(String userId, String eventId) {
        log.info("Deleting lifecycle event: {} for user: {}", eventId, userId);

        LifecycleEventEntity entity = lifecycleEventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("생애주기 이벤트를 찾을 수 없습니다"));

        // 권한 확인
        if (!entity.getUserId().equals(userId)) {
            throw new UnauthorizedException("FORBIDDEN", "해당 이벤트에 접근할 권한이 없습니다");
        }

        lifecycleEventRepository.delete(entity);

        // 로드맵 캐시 무효화
        invalidateRoadmapCache(userId);
    }

    @Override
    public long countByUserId(String userId) {
        return lifecycleEventRepository.countByUserId(userId);
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
