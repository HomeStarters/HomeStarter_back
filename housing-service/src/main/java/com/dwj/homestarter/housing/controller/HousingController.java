package com.dwj.homestarter.housing.controller;

import com.dwj.homestarter.common.dto.ApiResponse;
import com.dwj.homestarter.housing.config.jwt.UserPrincipal;
import com.dwj.homestarter.housing.dto.request.HousingCreateRequest;
import com.dwj.homestarter.housing.dto.request.HousingUpdateRequest;
import com.dwj.homestarter.housing.dto.response.GoalHousingResponse;
import com.dwj.homestarter.housing.dto.response.HousingDeleteResponse;
import com.dwj.homestarter.housing.dto.response.HousingListResponse;
import com.dwj.homestarter.housing.dto.response.HousingResponse;
import com.dwj.homestarter.housing.service.HousingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 주택 관리 Controller
 * 주택 등록, 조회, 수정, 삭제, 최종목표 설정 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/housings")
@RequiredArgsConstructor
@Tag(name = "Housing", description = "주택 관리 API")
public class HousingController {

    private final HousingService housingService;

    /**
     * 주택 등록
     */
    @PostMapping
    @Operation(summary = "주택 등록", description = "새로운 주택 정보를 등록합니다", operationId = "createHousing")
    public ResponseEntity<ApiResponse<HousingResponse>> createHousing(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody HousingCreateRequest request) {

        log.info("주택 등록 요청: userId={}", principal.getUserId());

        HousingResponse response = housingService.createHousing(principal.getUserId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("주택이 성공적으로 등록되었습니다", response));
    }

    /**
     * 주택 목록 조회
     */
    @GetMapping
    @Operation(summary = "주택 목록 조회", description = "사용자의 주택 목록을 조회합니다 (페이징)", operationId = "getHousings")
    public ResponseEntity<ApiResponse<HousingListResponse>> getHousings(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 (createdAt, price 등)") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "정렬 방향 (ASC, DESC)") @RequestParam(defaultValue = "DESC") String direction) {

        log.info("주택 목록 조회: userId={}, page={}, size={}", principal.getUserId(), page, size);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        HousingListResponse response = housingService.getHousings(principal.getUserId(), pageable);

        return ResponseEntity.ok(ApiResponse.success("주택 목록을 조회했습니다", response));
    }

    /**
     * 주택 상세 조회
     */
    @GetMapping("/{id}")
    @Operation(summary = "주택 상세 조회", description = "주택의 상세 정보를 조회합니다", operationId = "getHousing")
    public ResponseEntity<ApiResponse<HousingResponse>> getHousing(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        log.info("주택 상세 조회: housingId={}, userId={}", id, principal.getUserId());

        HousingResponse response = housingService.getHousing(id, principal.getUserId());

        return ResponseEntity.ok(ApiResponse.success("주택 정보를 조회했습니다", response));
    }

    /**
     * 주택 정보 수정
     */
    @PutMapping("/{id}")
    @Operation(summary = "주택 정보 수정", description = "주택 정보를 수정합니다", operationId = "updateHousing")
    public ResponseEntity<ApiResponse<HousingResponse>> updateHousing(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody HousingUpdateRequest request) {

        log.info("주택 정보 수정: housingId={}, userId={}", id, principal.getUserId());

        HousingResponse response = housingService.updateHousing(id, principal.getUserId(), request);

        return ResponseEntity.ok(ApiResponse.success("주택 정보가 수정되었습니다", response));
    }

    /**
     * 주택 삭제
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "주택 삭제", description = "주택을 삭제합니다", operationId = "deleteHousing")
    public ResponseEntity<ApiResponse<HousingDeleteResponse>> deleteHousing(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        log.info("주택 삭제: housingId={}, userId={}", id, principal.getUserId());

        HousingDeleteResponse response = housingService.deleteHousing(id, principal.getUserId());

        return ResponseEntity.ok(ApiResponse.success("주택이 삭제되었습니다", response));
    }

    /**
     * 최종목표 주택 설정
     */
    @PutMapping("/{id}/goal")
    @Operation(summary = "최종목표 주택 설정", description = "해당 주택을 최종목표 주택으로 설정합니다", operationId = "setGoalHousing")
    public ResponseEntity<ApiResponse<GoalHousingResponse>> setGoalHousing(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        log.info("최종목표 주택 설정: housingId={}, userId={}", id, principal.getUserId());

        GoalHousingResponse response = housingService.setGoalHousing(id, principal.getUserId());

        return ResponseEntity.ok(ApiResponse.success("최종목표 주택이 설정되었습니다", response));
    }
}
