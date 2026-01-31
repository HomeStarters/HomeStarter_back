package com.dwj.homestarter.roadmap.repository.jpa;

import com.dwj.homestarter.roadmap.repository.entity.RoadmapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 로드맵 리포지토리
 * 로드맵 데이터 접근을 담당
 */
@Repository
public interface RoadmapRepository extends JpaRepository<RoadmapEntity, String> {

    /**
     * 사용자별 최신 로드맵 조회
     *
     * @param userId 사용자 ID
     * @return 최신 로드맵 (Optional)
     */
    @Query("SELECT r FROM RoadmapEntity r WHERE r.userId = :userId ORDER BY r.version DESC LIMIT 1")
    Optional<RoadmapEntity> findLatestByUserId(@Param("userId") String userId);

    /**
     * 사용자별 특정 버전 로드맵 조회
     *
     * @param userId 사용자 ID
     * @param version 버전
     * @return 로드맵 (Optional)
     */
    Optional<RoadmapEntity> findByUserIdAndVersion(String userId, Integer version);

    /**
     * 사용자별 로드맵 버전 이력 조회 (최신순, 최대 3개)
     *
     * @param userId 사용자 ID
     * @return 로드맵 목록
     */
    @Query("SELECT r FROM RoadmapEntity r WHERE r.userId = :userId ORDER BY r.version DESC LIMIT 3")
    List<RoadmapEntity> findTop3ByUserIdOrderByVersionDesc(@Param("userId") String userId);

    /**
     * 작업 ID로 로드맵 조회
     *
     * @param taskId 작업 ID
     * @return 로드맵 (Optional)
     */
    Optional<RoadmapEntity> findByTaskId(String taskId);

    /**
     * 사용자별 최대 버전 조회
     *
     * @param userId 사용자 ID
     * @return 최대 버전 (Optional)
     */
    @Query("SELECT MAX(r.version) FROM RoadmapEntity r WHERE r.userId = :userId")
    Optional<Integer> findMaxVersionByUserId(@Param("userId") String userId);

    /**
     * 사용자별 특정 상태의 최신 로드맵 조회
     *
     * @param userId 사용자 ID
     * @param status 로드맵 상태
     * @return 최신 로드맵 (Optional)
     */
    @Query("SELECT r FROM RoadmapEntity r WHERE r.userId = :userId AND r.status = :status ORDER BY r.version DESC LIMIT 1")
    Optional<RoadmapEntity> findTopByUserIdAndStatusOrderByVersionDesc(@Param("userId") String userId,
                                                                        @Param("status") com.dwj.homestarter.roadmap.repository.entity.RoadmapStatus status);

    /**
     * 사용자별 로드맵 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    boolean existsByUserId(String userId);
}
