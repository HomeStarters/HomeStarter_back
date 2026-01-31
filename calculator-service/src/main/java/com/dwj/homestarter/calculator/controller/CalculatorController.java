package com.dwj.homestarter.calculator.controller;

import com.dwj.homestarter.calculator.dto.request.HousingExpensesRequest;
import com.dwj.homestarter.calculator.dto.response.CalculationResultListResponse;
import com.dwj.homestarter.calculator.dto.response.CalculationResultResponse;
import com.dwj.homestarter.calculator.service.ExpenseCalculatorService;
import com.dwj.homestarter.calculator.config.jwt.UserPrincipal;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 계산기 컨트롤러
 * REST API 엔드포인트 처리, 요청/응답 변환, HTTP 프로토콜 처리
 */
@Slf4j
@RestController
@RequestMapping("/calculator")
@RequiredArgsConstructor
@Tag(name = "Calculator", description = "재무 계산 API")
public class CalculatorController {

    private final ExpenseCalculatorService expenseCalculatorService;

    /**
     * 입주 후 지출 계산
     *
     * @param request        계산 요청
     * @param authentication 인증 정보
     * @return 계산 결과
     */
    @PostMapping("/housing-expenses")
    @Operation(summary = "입주 후 지출 계산", description = "주택 입주 후 예상 지출을 계산합니다")
    public ResponseEntity<CalculationResultResponse> calculateHousingExpenses(
            @Valid @RequestBody HousingExpensesRequest request,
            Authentication authentication) {

        String userId = ((UserPrincipal) authentication.getPrincipal()).getUserId();
        log.info("입주 후 지출 계산 요청 - userId: {}, housingId: {}", userId, request.getHousingId());

        CalculationResultResponse response = expenseCalculatorService.calculateHousingExpenses(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 계산 결과 목록 조회
     *
     * @param housingId      주택 ID (optional)
     * @param status         상태 (optional, ELIGIBLE/INELIGIBLE)
     * @param sortBy         정렬 기준 (default: calculatedAt)
     * @param sortOrder      정렬 순서 (default: desc)
     * @param page           페이지 번호 (default: 0)
     * @param size           페이지 크기 (default: 20)
     * @param authentication 인증 정보
     * @return 계산 결과 목록
     */
    @GetMapping("/results")
    @Operation(summary = "계산 결과 목록 조회", description = "사용자의 계산 결과 목록을 조회합니다")
    public ResponseEntity<CalculationResultListResponse> getCalculationResults(
            @Parameter(description = "주택 ID") @RequestParam(required = false) String housingId,
            @Parameter(description = "상태 (ELIGIBLE/INELIGIBLE)") @RequestParam(required = false) String status,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "calculatedAt") String sortBy,
            @Parameter(description = "정렬 순서") @RequestParam(defaultValue = "desc") String sortOrder,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String userId = ((UserPrincipal) authentication.getPrincipal()).getUserId();
        log.info("계산 결과 목록 조회 - userId: {}, housingId: {}, status: {}", userId, housingId, status);

        Sort sort = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        CalculationResultListResponse response = expenseCalculatorService.getCalculationResults(
                userId, housingId, status, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 계산 결과 상세 조회
     *
     * @param id             계산 결과 ID
     * @param authentication 인증 정보
     * @return 계산 결과 상세
     */
    @GetMapping("/results/{id}")
    @Operation(summary = "계산 결과 상세 조회", description = "특정 계산 결과의 상세 정보를 조회합니다")
    public ResponseEntity<CalculationResultResponse> getCalculationResult(
            @Parameter(description = "계산 결과 ID") @PathVariable String id,
            Authentication authentication) {

        String userId = ((UserPrincipal) authentication.getPrincipal()).getUserId();
        log.info("계산 결과 상세 조회 - id: {}, userId: {}", id, userId);

        CalculationResultResponse response = expenseCalculatorService.getCalculationResult(id, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 계산 결과 삭제
     *
     * @param id             계산 결과 ID
     * @param authentication 인증 정보
     * @return 204 No Content
     */
    @DeleteMapping("/results/{id}")
    @Operation(summary = "계산 결과 삭제", description = "특정 계산 결과를 삭제합니다")
    public ResponseEntity<Void> deleteCalculationResult(
            @Parameter(description = "계산 결과 ID") @PathVariable String id,
            Authentication authentication) {

        String userId = ((UserPrincipal) authentication.getPrincipal()).getUserId();
        log.info("계산 결과 삭제 - id: {}, userId: {}", id, userId);

        expenseCalculatorService.deleteCalculationResult(id, userId);
        return ResponseEntity.noContent().build();
    }
}
