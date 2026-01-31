package com.dwj.homestarter.roadmap.repository.jpa;

import com.dwj.homestarter.roadmap.repository.entity.RoadmapTaskEntity;
import com.dwj.homestarter.roadmap.repository.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 비동기 작업 리포지토리
 * 비동기 작업 데이터 접근을 담당
 */
@Repository
public interface RoadmapTaskRepository extends JpaRepository<RoadmapTaskEntity, String> {

    /**
     * 진행 중 작업 조회 (PENDING 또는 PROCESSING 상태)
     *
     * @param userId 사용자 ID
     * @return 진행 중 작업 목록
     */
    @Query("SELECT t FROM RoadmapTaskEntity t WHERE t.userId = :userId " +
           "AND (t.status = 'PENDING' OR t.status = 'PROCESSING') " +
           "ORDER BY t.createdAt DESC")
    List<RoadmapTaskEntity> findActiveTasksByUserId(@Param("userId") String userId);

    /**
     * 특정 상태의 작업 조회
     *
     * @param userId 사용자 ID
     * @param status 작업 상태
     * @return 작업 목록
     */
    List<RoadmapTaskEntity> findByUserIdAndStatus(String userId, TaskStatus status);

    /**
     * 작업 ID로 조회 (사용자 확인 포함)
     *
     * @param id 작업 ID
     * @param userId 사용자 ID
     * @return 작업 (Optional)
     */
    Optional<RoadmapTaskEntity> findByIdAndUserId(String id, String userId);

    /**
     * 여러 상태의 작업 조회
     *
     * @param userId 사용자 ID
     * @param statuses 작업 상태 목록
     * @return 작업 목록
     */
    List<RoadmapTaskEntity> findByUserIdAndStatusIn(String userId, List<TaskStatus> statuses);
}
