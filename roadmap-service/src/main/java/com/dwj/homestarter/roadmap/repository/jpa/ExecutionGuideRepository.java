package com.dwj.homestarter.roadmap.repository.jpa;

import com.dwj.homestarter.roadmap.repository.entity.ExecutionGuideEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 실행 가이드 리포지토리
 * 실행 가이드 데이터 접근을 담당
 */
@Repository
public interface ExecutionGuideRepository extends JpaRepository<ExecutionGuideEntity, String> {

    /**
     * 로드맵별 실행 가이드 조회
     *
     * @param roadmapId 로드맵 ID
     * @return 실행 가이드 (Optional)
     */
    Optional<ExecutionGuideEntity> findByRoadmapId(String roadmapId);

    /**
     * 로드맵별 실행 가이드 삭제
     *
     * @param roadmapId 로드맵 ID
     */
    void deleteByRoadmapId(String roadmapId);
}
