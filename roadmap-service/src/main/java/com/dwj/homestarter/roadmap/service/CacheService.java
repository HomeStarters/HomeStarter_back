package com.dwj.homestarter.roadmap.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 캐시 서비스
 * Redis 캐시 관리 및 무효화 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 특정 키의 캐시 삭제
     *
     * @param key 캐시 키
     */
    public void delete(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("캐시 삭제 완료 - Key: {}", key);
            }
        } catch (Exception e) {
            log.error("캐시 삭제 중 오류 발생 - Key: {}", key, e);
            throw e;
        }
    }

    /**
     * 패턴에 매칭되는 모든 캐시 삭제
     * Redis KEYS 명령어로 매칭되는 키를 찾아 삭제
     *
     * @param pattern 캐시 키 패턴 (예: "roadmap:*", "roadmap:user123:*")
     * @return 삭제된 캐시 개수
     */
    public Long deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deletedCount = redisTemplate.delete(keys);
                log.debug("패턴 캐시 삭제 완료 - Pattern: {}, Count: {}", pattern, deletedCount);
                return deletedCount != null ? deletedCount : 0L;
            }
            log.debug("삭제할 캐시 없음 - Pattern: {}", pattern);
            return 0L;
        } catch (Exception e) {
            log.error("패턴 캐시 삭제 중 오류 발생 - Pattern: {}", pattern, e);
            throw e;
        }
    }

    /**
     * 사용자 ID로 로드맵 캐시 무효화
     * roadmap:{userId} 패턴의 캐시 삭제
     *
     * @param userId 사용자 ID
     * @return 삭제된 캐시 개수
     */
    public Long invalidateRoadmapCacheByUserId(String userId) {
        String pattern = "roadmap:" + userId;
        return deletePattern(pattern);
    }

    /**
     * 전체 로드맵 캐시 무효화
     * roadmap:* 패턴의 모든 캐시 삭제
     *
     * @return 삭제된 캐시 개수
     */
    public Long invalidateAllRoadmapCache() {
        String pattern = "roadmap:*";
        return deletePattern(pattern);
    }
}
