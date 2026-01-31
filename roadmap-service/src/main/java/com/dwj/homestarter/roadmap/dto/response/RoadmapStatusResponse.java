package com.dwj.homestarter.roadmap.dto.response;

import com.dwj.homestarter.roadmap.repository.entity.TaskStatus;
import lombok.*;

/**
 * 로드맵 생성/재설계 상태 조회 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapStatusResponse {

    /**
     * 작업 요청 ID
     */
    private String requestId;

    /**
     * 작업 상태
     */
    private TaskStatus status;

    /**
     * 진행률 (0-100)
     */
    private Integer progress;

    /**
     * 상태 메시지
     */
    private String message;

    /**
     * 완료된 로드맵 ID (status=COMPLETED일 때만)
     */
    private String roadmapId;

    /**
     * 에러 메시지 (status=FAILED일 때만)
     */
    private String error;
}
