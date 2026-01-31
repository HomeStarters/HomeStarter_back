package com.dwj.homestarter.roadmap.dto.response;

import com.dwj.homestarter.roadmap.repository.entity.TaskStatus;
import lombok.*;

/**
 * 로드맵 생성 작업 응답 DTO (202 Accepted)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapTaskResponse {

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
     * 예상 완료 시간 (초)
     */
    private Integer estimatedCompletionTime;
}
