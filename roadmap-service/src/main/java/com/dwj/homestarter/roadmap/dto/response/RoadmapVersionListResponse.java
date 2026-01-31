package com.dwj.homestarter.roadmap.dto.response;

import lombok.*;

import java.util.List;

/**
 * 로드맵 버전 이력 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapVersionListResponse {

    /**
     * 버전 목록 (최대 3개)
     */
    private List<RoadmapVersionInfo> versions;
}
