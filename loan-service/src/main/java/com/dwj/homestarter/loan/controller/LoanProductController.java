package com.dwj.homestarter.loan.controller;

import com.dwj.homestarter.loan.dto.LoanProductListResponse;
import com.dwj.homestarter.loan.dto.LoanProductResponse;
import com.dwj.homestarter.loan.service.LoanProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 대출상품 Controller (일반 사용자용)
 *
 * 일반 사용자를 위한 대출상품 조회 API
 *
 * @author homestarter
 * @since 1.0.0
 */
@Tag(name = "Loan Product", description = "대출상품 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanProductController {

    private final LoanProductService loanProductService;

    /**
     * 대출상품 목록 조회
     *
     * @param housingType 주택유형 필터 (옵션)
     * @param sortBy 정렬 기준 (기본: createdAt)
     * @param sortOrder 정렬 순서 (기본: desc)
     * @param keyword 검색 키워드 (옵션)
     * @param page 페이지 번호 (기본: 0)
     * @param size 페이지 크기 (기본: 20)
     * @return 대출상품 목록
     */
    @Operation(summary = "대출상품 목록 조회", description = "필터링, 정렬, 검색, 페이징을 지원하는 대출상품 목록 조회")
    @GetMapping
    public ResponseEntity<LoanProductListResponse> getLoanProducts(
            @Parameter(description = "주택유형 필터") @RequestParam(required = false) String housingType,
            @Parameter(description = "정렬 기준 (createdAt, interestRate, loanLimit)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 순서 (asc, desc)") @RequestParam(defaultValue = "desc") String sortOrder,
            @Parameter(description = "검색 키워드 (대출이름, 대상주택)") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        // 정렬 설정
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        LoanProductListResponse response = loanProductService.getLoanProducts(
                housingType, sortBy, sortOrder, keyword, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * 대출상품 상세 조회
     *
     * @param id 대출상품 ID
     * @return 대출상품 상세 정보
     */
    @Operation(summary = "대출상품 상세 조회", description = "대출상품 ID로 상세 정보 조회")
    @GetMapping("/{id}")
    public ResponseEntity<LoanProductResponse> getLoanProductDetail(
            @Parameter(description = "대출상품 ID") @PathVariable Long id) {

        LoanProductResponse response = loanProductService.getLoanProductById(id);
        return ResponseEntity.ok(response);
    }
}
