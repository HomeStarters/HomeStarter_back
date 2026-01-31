package com.dwj.homestarter.roadmap.repository.jpa;

import com.dwj.homestarter.roadmap.repository.entity.RoadmapStageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 로드맵 단계 리포지토리
 * 로드맵 단계 데이터 접근을 담당
 */
@Repository
public interface RoadmapStageRepository extends JpaRepository<RoadmapStageEntity, String> {

    /**
     * 로드맵별 단계 목록 조회 (순서 정렬)
     *
     * @param roadmapId 로드맵 ID
     * @return 단계 목록
     */
    List<RoadmapStageEntity> findByRoadmapIdOrderByStageNumberAsc(String roadmapId);

    /**
     * 로드맵별 단계 삭제
     *
     * @param roadmapId 로드맵 ID
     */
    void deleteByRoadmapId(String roadmapId);
}
