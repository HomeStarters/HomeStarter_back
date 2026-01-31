package com.dwj.homestarter.roadmap.dto.response;

import lombok.*;

import java.util.List;

/**
 * 생애주기 이벤트 목록 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LifecycleEventListResponse {

    /**
     * 이벤트 목록
     */
    private List<LifecycleEventResponse> events;

    /**
     * 전체 개수
     */
    private Integer total;
}
