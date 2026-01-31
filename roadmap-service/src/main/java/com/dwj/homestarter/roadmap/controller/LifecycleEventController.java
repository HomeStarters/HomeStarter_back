package com.dwj.homestarter.roadmap.controller;

import com.dwj.homestarter.roadmap.dto.request.LifecycleEventRequest;
import com.dwj.homestarter.roadmap.dto.response.LifecycleEventListResponse;
import com.dwj.homestarter.roadmap.dto.response.LifecycleEventResponse;
import com.dwj.homestarter.roadmap.repository.entity.EventType;
import com.dwj.homestarter.roadmap.service.LifecycleEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 생애주기 이벤트 관리 컨트롤러
 * 생애주기 이벤트 등록, 조회, 수정, 삭제 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/lifecycle-events")
@RequiredArgsConstructor
@Tag(name = "LifecycleEvents", description = "생애주기 이벤트 관리")
public class LifecycleEventController {

    private final LifecycleEventService lifecycleEventService;

    /**
     * 생애주기 이벤트 등록
     *
     * @param userId 사용자 ID (JWT 토큰에서 추출)
     * @param request 이벤트 등록 요청
     * @return 등록된 이벤트 정보
     */
    @PostMapping
    @Operation(summary = "생애주기 이벤트 등록", description = "새로운 생애주기 이벤트를 등록합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "이벤트 등록 성공",
                    content = @Content(schema = @Schema(implementation = LifecycleEventResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<LifecycleEventResponse> createLifecycleEvent(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody LifecycleEventRequest request) {

        log.info("Creating lifecycle event for user: {}, event: {}", userId, request.getName());

        LifecycleEventResponse response = lifecycleEventService.createLifecycleEvent(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 생애주기 이벤트 목록 조회
     *
     * @param userId 사용자 ID (JWT 토큰에서 추출)
     * @param eventType 이벤트 유형 필터 (선택)
     * @return 이벤트 목록
     */
    @GetMapping
    @Operation(summary = "생애주기 이벤트 목록 조회", description = "등록된 모든 생애주기 이벤트를 조회합니다 (날짜순 정렬)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이벤트 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = LifecycleEventListResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<LifecycleEventListResponse> getLifecycleEvents(
            @AuthenticationPrincipal String userId,
            @Parameter(description = "이벤트 유형 필터")
            @RequestParam(required = false) EventType eventType) {

        log.info("Getting lifecycle events for user: {}, eventType: {}", userId, eventType);

        LifecycleEventListResponse response = lifecycleEventService.getLifecycleEvents(userId, eventType);

        return ResponseEntity.ok(response);
    }

    /**
     * 생애주기 이벤트 상세 조회
     *
     * @param userId 사용자 ID (JWT 토큰에서 추출)
     * @param id 이벤트 ID
     * @return 이벤트 상세 정보
     */
    @GetMapping("/{id}")
    @Operation(summary = "생애주기 이벤트 상세 조회", description = "특정 이벤트의 상세 정보를 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이벤트 조회 성공",
                    content = @Content(schema = @Schema(implementation = LifecycleEventResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    public ResponseEntity<LifecycleEventResponse> getLifecycleEvent(
            @AuthenticationPrincipal String userId,
            @Parameter(description = "이벤트 ID", required = true)
            @PathVariable String id) {

        log.info("Getting lifecycle event: {} for user: {}", id, userId);

        LifecycleEventResponse response = lifecycleEventService.getLifecycleEvent(userId, id);

        return ResponseEntity.ok(response);
    }

    /**
     * 생애주기 이벤트 수정
     *
     * @param userId 사용자 ID (JWT 토큰에서 추출)
     * @param id 이벤트 ID
     * @param request 이벤트 수정 요청
     * @return 수정된 이벤트 정보
     */
    @PutMapping("/{id}")
    @Operation(summary = "생애주기 이벤트 수정", description = "이벤트 정보를 수정합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이벤트 수정 성공",
                    content = @Content(schema = @Schema(implementation = LifecycleEventResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    public ResponseEntity<LifecycleEventResponse> updateLifecycleEvent(
            @AuthenticationPrincipal String userId,
            @Parameter(description = "이벤트 ID", required = true)
            @PathVariable String id,
            @Valid @RequestBody LifecycleEventRequest request) {

        log.info("Updating lifecycle event: {} for user: {}", id, userId);

        LifecycleEventResponse response = lifecycleEventService.updateLifecycleEvent(userId, id, request);

        return ResponseEntity.ok(response);
    }

    /**
     * 생애주기 이벤트 삭제
     *
     * @param userId 사용자 ID (JWT 토큰에서 추출)
     * @param id 이벤트 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "생애주기 이벤트 삭제", description = "이벤트를 삭제합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "이벤트 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    public ResponseEntity<Void> deleteLifecycleEvent(
            @AuthenticationPrincipal String userId,
            @Parameter(description = "이벤트 ID", required = true)
            @PathVariable String id) {

        log.info("Deleting lifecycle event: {} for user: {}", id, userId);

        lifecycleEventService.deleteLifecycleEvent(userId, id);

        return ResponseEntity.noContent().build();
    }
}
