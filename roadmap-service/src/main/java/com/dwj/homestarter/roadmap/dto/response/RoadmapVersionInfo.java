package com.dwj.homestarter.roadmap.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 로드맵 버전 정보 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapVersionInfo {

    /**
     * 버전 번호
     */
    private Integer version;

    /**
     * 생성일시
     */
    private LocalDateTime createdAt;

    /**
     * 변경 설명
     */
    private String changeDescription;
}
