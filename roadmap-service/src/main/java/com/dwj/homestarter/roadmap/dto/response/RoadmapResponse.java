package com.dwj.homestarter.roadmap.dto.response;

import com.dwj.homestarter.roadmap.repository.entity.RoadmapStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 로드맵 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapResponse {

    /**
     * 로드맵 ID
     */
    private String id;

    /**
     * 사용자 ID
     */
    private String userId;

    /**
     * 로드맵 버전
     */
    private Integer version;

    /**
     * 로드맵 상태
     */
    private RoadmapStatus status;

    /**
     * 최종목표 주택 정보
     */
    private GoalHousingDto goalHousing;

    /**
     * 단계별 계획 목록
     */
    private List<RoadmapStageDto> stages;

    /**
     * 실행 가이드
     */
    private ExecutionGuideDto executionGuide;

    /**
     * 생성일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    private LocalDateTime updatedAt;
}
