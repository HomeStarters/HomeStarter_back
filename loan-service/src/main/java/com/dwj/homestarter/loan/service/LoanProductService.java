package com.dwj.homestarter.loan.service;

import com.dwj.homestarter.loan.dto.*;
import org.springframework.data.domain.Pageable;

/**
 * 대출상품 서비스 인터페이스
 *
 * 대출상품 비즈니스 로직 처리 인터페이스
 *
 * @author homestarter
 * @since 1.0.0
 */
public interface LoanProductService {

    /**
     * 대출상품 목록 조회
     *
     * @param housingType 주택유형 필터
     * @param sortBy 정렬 기준
     * @param sortOrder 정렬 순서
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 대출상품 목록 응답
     */
    LoanProductListResponse getLoanProducts(String housingType, String sortBy, String sortOrder,
                                           String keyword, Pageable pageable);

    /**
     * 대출상품 상세 조회
     *
     * @param id 대출상품 ID
     * @return 대출상품 응답
     */
    LoanProductResponse getLoanProductById(Long id);

    /**
     * 대출상품 등록
     *
     * @param request 대출상품 등록 요청
     * @return 대출상품 응답
     */
    LoanProductResponse createLoanProduct(CreateLoanProductRequest request);

    /**
     * 대출상품 수정
     *
     * @param id 대출상품 ID
     * @param request 대출상품 수정 요청
     * @return 대출상품 응답
     */
    LoanProductResponse updateLoanProduct(Long id, UpdateLoanProductRequest request);

    /**
     * 대출상품 삭제 (소프트 삭제)
     *
     * @param id 대출상품 ID
     */
    void deleteLoanProduct(Long id);
}
