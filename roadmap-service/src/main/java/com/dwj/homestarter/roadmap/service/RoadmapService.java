package com.dwj.homestarter.roadmap.service;

import com.dwj.homestarter.roadmap.dto.response.RoadmapResponse;
import com.dwj.homestarter.roadmap.dto.response.RoadmapStatusResponse;
import com.dwj.homestarter.roadmap.dto.response.RoadmapTaskResponse;
import com.dwj.homestarter.roadmap.dto.response.RoadmapVersionListResponse;

/**
 * 로드맵 서비스 인터페이스
 */
public interface RoadmapService {

    /**
     * 로드맵 생성 요청 (비동기)
     *
     * @param userId 사용자 ID
     * @param authorization JWT 토큰
     * @return 작업 요청 정보 (202 Accepted)
     */
    RoadmapTaskResponse generateRoadmap(String userId, String authorization);

    /**
     * 로드맵 조회
     *
     * @param userId 사용자 ID
     * @param version 버전 (null이면 최신 버전)
     * @return 로드맵 정보
     */
    RoadmapResponse getRoadmap(String userId, Integer version);

    /**
     * 로드맵 재설계 요청 (비동기)
     *
     * @param userId 사용자 ID
     * @param authorization JWT 토큰
     * @return 작업 요청 정보 (202 Accepted)
     */
    RoadmapTaskResponse regenerateRoadmap(String userId, String authorization);

    /**
     * 로드맵 생성/재설계 상태 조회
     *
     * @param userId 사용자 ID
     * @param requestId 요청 ID
     * @return 작업 상태 정보
     */
    RoadmapStatusResponse getRoadmapStatus(String userId, String requestId);

    /**
     * 로드맵 버전 이력 조회
     *
     * @param userId 사용자 ID
     * @return 버전 목록 (최대 3개)
     */
    RoadmapVersionListResponse getRoadmapVersions(String userId);
}
