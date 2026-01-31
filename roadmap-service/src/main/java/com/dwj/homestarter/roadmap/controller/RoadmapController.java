package com.dwj.homestarter.roadmap.controller;

import com.dwj.homestarter.roadmap.dto.response.RoadmapResponse;
import com.dwj.homestarter.roadmap.dto.response.RoadmapStatusResponse;
import com.dwj.homestarter.roadmap.dto.response.RoadmapTaskResponse;
import com.dwj.homestarter.roadmap.dto.response.RoadmapVersionListResponse;
import com.dwj.homestarter.roadmap.service.RoadmapService;
import com.dwj.homestarter.roadmap.service.sse.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 장기주거 로드맵 관리 컨트롤러
 * 로드맵 생성, 조회, 재설계, 상태 조회, 버전 관리 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/roadmaps")
@RequiredArgsConstructor
@Tag(name = "Roadmap", description = "장기주거 로드맵")
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final SseService sseService;

    /**
     * 장기주거 로드맵 생성 요청 (비동기)
     * AI를 활용하여 장기주거 로드맵을 생성합니다
     *
     * @param userId 사용자 ID (JWT 토큰에서 추출)
     * @param authorization JWT 토큰 (외부 서비스 호출 시 사용)
     * @return 로드맵 생성 요청 정보 (202 Accepted)
     */
    @PostMapping
    @Operation(summary = "장기주거 로드맵 생성 요청", description = "AI를 활용하여 장기주거 로드맵을 생성합니다 (비동기 처리)")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "로드맵 생성 요청 접수",
                    content = @Content(schema = @Schema(implementation = RoadmapTaskResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (최종목표 주택 미설정 등)"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<RoadmapTaskResponse> createRoadmap(
            @AuthenticationPrincipal String userId,
            @RequestHeader("Authorization") String authorization) {

        log.info("Creating roadmap for user: {}", userId);

        RoadmapTaskResponse response = roadmapService.generateRoadmap(userId, authorization);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * 장기주거 로드맵 조회
     * 생성된 장기주거 로드맵을 조회합니다
     *
     * @param userId 사용자 ID (JWT 토큰에서 추출)
     * @param version 로드맵 버전 (선택, 미지정 시 최신 버전)
     * @return 로드맵 정보
     */
    @GetMapping
    @Operation(summary = "장기주거 로드맵 조회", description = "생성된 장기주거 로드맵을 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로드맵 조회 성공",
                    content = @Content(schema = @Schema(implementation = RoadmapResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "로드맵이 생성되지 않음")
    })
    public ResponseEntity<RoadmapResponse> getRoadmap(
            @AuthenticationPrincipal String userId,
            @Parameter(description = "로드맵 버전 (미지정 시 최신 버전)")
            @RequestParam(required = false) Integer version) {

        log.info("Getting roadmap for user: {}, version: {}", userId, version);

        RoadmapResponse response = roadmapService.getRoadmap(userId, version);

        return ResponseEntity.ok(response);
    }

    /**
     * 장기주거 로드맵 재설계 (비동기)
     * 변경된 정보를 반영하여 로드맵을 재설계합니다
     *
     * @param userId 사용자 ID (JWT 토큰에서 추출)
     * @param authorization JWT 토큰 (외부 서비스 호출 시 사용)
     * @return 로드맵 재설계 요청 정보 (202 Accepted)
     */
    @PutMapping
    @Operation(summary = "장기주거 로드맵 재설계", description = "변경된 정보를 반영하여 로드맵을 재설계합니다 (비동기 처리)")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "로드맵 재설계 요청 접수",
                    content = @Content(schema = @Schema(implementation = RoadmapTaskResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "기존 로드맵이 없음")
    })
    public ResponseEntity<RoadmapTaskResponse> updateRoadmap(
            @AuthenticationPrincipal String userId,
            @RequestHeader("Authorization") String authorization) {

        log.info("Updating roadmap for user: {}", userId);

        RoadmapTaskResponse response = roadmapService.regenerateRoadmap(userId, authorization);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * 로드맵 생성/재설계 상태 조회
     * 비동기 로드맵 생성/재설계 요청의 처리 상태를 조회합니다
     *
     * @param userId 사용자 ID (JWT 토큰에서 추출)
     * @param requestId 로드맵 요청 ID
     * @return 처리 상태 정보
     */
    @GetMapping("/status/{requestId}")
    @Operation(summary = "로드맵 생성/재설계 상태 조회",
            description = "비동기 로드맵 생성/재설계 요청의 처리 상태를 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 조회 성공",
                    content = @Content(schema = @Schema(implementation = RoadmapStatusResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "요청을 찾을 수 없음")
    })
    public ResponseEntity<RoadmapStatusResponse> getRoadmapStatus(
            @AuthenticationPrincipal String userId,
            @Parameter(description = "로드맵 요청 ID", required = true)
            @PathVariable String requestId) {

        log.info("Getting roadmap status: {} for user: {}", requestId, userId);

        RoadmapStatusResponse response = roadmapService.getRoadmapStatus(userId, requestId);

        return ResponseEntity.ok(response);
    }

    /**
     * 로드맵 버전 이력 조회
     * 저장된 로드맵 버전 목록을 조회합니다 (최대 3개)
     *
     * @param userId 사용자 ID (JWT 토큰에서 추출)
     * @return 버전 목록 (최대 3개)
     */
    @GetMapping("/versions")
    @Operation(summary = "로드맵 버전 이력 조회", description = "저장된 로드맵 버전 목록을 조회합니다 (최대 3개)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "버전 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = RoadmapVersionListResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<RoadmapVersionListResponse> getRoadmapVersions(
            @AuthenticationPrincipal String userId) {

        log.info("Getting roadmap versions for user: {}", userId);

        RoadmapVersionListResponse response = roadmapService.getRoadmapVersions(userId);

        return ResponseEntity.ok(response);
    }

    /**
     * 로드맵 생성 진행 상황 스트리밍 (SSE)
     * Server-Sent Events를 통해 실시간 진행 상황을 스트리밍합니다
     *
     * @param taskId 작업 ID
     * @return SSE Emitter
     */
    @GetMapping(value = "/tasks/{taskId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "로드맵 생성 진행 상황 스트리밍",
            description = "Server-Sent Events를 통해 실시간 진행 상황을 스트리밍합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SSE 스트리밍 시작"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public SseEmitter streamRoadmapProgress(
            @Parameter(description = "작업 ID", required = true)
            @PathVariable String taskId) {

        log.info("Starting SSE stream for task: {}", taskId);

        return sseService.createEmitter(taskId);
    }
}
