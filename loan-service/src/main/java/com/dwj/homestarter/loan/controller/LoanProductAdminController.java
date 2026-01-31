package com.dwj.homestarter.loan.controller;

import com.dwj.homestarter.common.dto.ApiResponse;
import com.dwj.homestarter.loan.dto.CreateLoanProductRequest;
import com.dwj.homestarter.loan.dto.LoanProductResponse;
import com.dwj.homestarter.loan.dto.UpdateLoanProductRequest;
import com.dwj.homestarter.loan.service.LoanProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 대출상품 관리 Controller (관리자용)
 *
 * 관리자를 위한 대출상품 관리 API
 *
 * @author homestarter
 * @since 1.0.0
 */
@Tag(name = "Loan Product Admin", description = "대출상품 관리 API (관리자)")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/loans")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class LoanProductAdminController {

    private final LoanProductService loanProductService;

    /**
     * 대출상품 등록
     *
     * @param request 대출상품 등록 요청
     * @return 등록된 대출상품 정보
     */
    @Operation(summary = "대출상품 등록", description = "새로운 대출상품 등록 (관리자 전용)")
    @PostMapping
    public ResponseEntity<LoanProductResponse> createLoanProduct(
            @Valid @RequestBody CreateLoanProductRequest request) {

        LoanProductResponse response = loanProductService.createLoanProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 대출상품 수정
     *
     * @param id 대출상품 ID
     * @param request 대출상품 수정 요청
     * @return 수정된 대출상품 정보
     */
    @Operation(summary = "대출상품 수정", description = "기존 대출상품 정보 수정 (관리자 전용)")
    @PutMapping("/{id}")
    public ResponseEntity<LoanProductResponse> updateLoanProduct(
            @Parameter(description = "대출상품 ID") @PathVariable Long id,
            @Valid @RequestBody UpdateLoanProductRequest request) {

        LoanProductResponse response = loanProductService.updateLoanProduct(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 대출상품 삭제
     *
     * @param id 대출상품 ID
     * @return 삭제 성공 메시지
     */
    @Operation(summary = "대출상품 삭제", description = "대출상품 소프트 삭제 (관리자 전용)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteLoanProduct(
            @Parameter(description = "대출상품 ID") @PathVariable Long id) {

        loanProductService.deleteLoanProduct(id);
        return ResponseEntity.ok(ApiResponse.success("대출상품이 삭제되었습니다"));
    }
}
