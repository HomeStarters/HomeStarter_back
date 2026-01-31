package com.dwj.homestarter.calculator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * 캐시 서비스
 * Redis 캐시 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 캐시에서 값 조회
     *
     * @param key 캐시 키
     * @return 캐시 값
     */
    public Optional<Object> get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("캐시 히트: {}", key);
            } else {
                log.debug("캐시 미스: {}", key);
            }
            return Optional.ofNullable(value);
        } catch (Exception e) {
            log.error("캐시 조회 실패: {}", key, e);
            return Optional.empty();
        }
    }

    /**
     * 캐시에 값 저장
     *
     * @param key   캐시 키
     * @param value 캐시 값
     * @param ttl   TTL (Time To Live)
     */
    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("캐시 저장: {} (TTL: {}초)", key, ttl.getSeconds());
        } catch (Exception e) {
            log.error("캐시 저장 실패: {}", key, e);
        }
    }

    /**
     * 캐시에서 값 삭제
     *
     * @param key 캐시 키
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("캐시 삭제: {}", key);
        } catch (Exception e) {
            log.error("캐시 삭제 실패: {}", key, e);
        }
    }

    /**
     * 패턴에 매칭되는 모든 캐시 삭제
     *
     * @param pattern 패턴 (예: "calc:*")
     * @return 삭제된 키 개수
     */
    public Long deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                log.debug("패턴 캐시 삭제: {} ({}개)", pattern, deleted);
                return deleted;
            }
            return 0L;
        } catch (Exception e) {
            log.error("패턴 캐시 삭제 실패: {}", pattern, e);
            return 0L;
        }
    }
}
