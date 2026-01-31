package com.dwj.homestarter.roadmap.repository.jpa;

import com.dwj.homestarter.roadmap.repository.entity.EventType;
import com.dwj.homestarter.roadmap.repository.entity.LifecycleEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 생애주기 이벤트 리포지토리
 * 생애주기 이벤트 데이터 접근을 담당
 */
@Repository
public interface LifecycleEventRepository extends JpaRepository<LifecycleEventEntity, String> {

    /**
     * 사용자별 이벤트 목록 조회 (생성일 역순)
     *
     * @param userId 사용자 ID
     * @return 이벤트 목록
     */
    List<LifecycleEventEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * 사용자별 이벤트 유형 필터링 조회
     *
     * @param userId 사용자 ID
     * @param eventType 이벤트 유형
     * @return 이벤트 목록
     */
    List<LifecycleEventEntity> findByUserIdAndEventTypeOrderByEventDateAsc(String userId, EventType eventType);

    /**
     * 사용자별 이벤트 목록 조회 (이벤트 날짜 오름차순)
     *
     * @param userId 사용자 ID
     * @return 이벤트 목록
     */
    List<LifecycleEventEntity> findByUserIdOrderByEventDateAsc(String userId);

    /**
     * 사용자별 이벤트 개수 조회
     *
     * @param userId 사용자 ID
     * @return 이벤트 개수
     */
    long countByUserId(String userId);

    /**
     * 특정 이벤트 조회 (사용자 확인 포함)
     *
     * @param id 이벤트 ID
     * @param userId 사용자 ID
     * @return 이벤트 (Optional)
     */
    Optional<LifecycleEventEntity> findByIdAndUserId(String id, String userId);

    /**
     * 사용자별 이벤트 삭제
     *
     * @param id 이벤트 ID
     * @param userId 사용자 ID
     */
    void deleteByIdAndUserId(String id, String userId);
}
