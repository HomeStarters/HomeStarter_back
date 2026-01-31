package com.dwj.homestarter.loan.repository;

import com.dwj.homestarter.loan.domain.LoanProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 대출상품 Repository
 *
 * 대출상품 데이터 접근 인터페이스
 *
 * @author homestarter
 * @since 1.0.0
 */
@Repository
public interface LoanProductRepository extends JpaRepository<LoanProduct, Long> {

    /**
     * 활성화된 대출상품 목록 조회
     *
     * @param pageable 페이징 정보
     * @return 활성화된 대출상품 페이지
     */
    Page<LoanProduct> findByActiveTrue(Pageable pageable);

    /**
     * 대상주택으로 필터링
     *
     * @param targetHousing 대상주택 키워드
     * @param pageable 페이징 정보
     * @return 필터링된 대출상품 페이지
     */
    Page<LoanProduct> findByTargetHousingContaining(String targetHousing, Pageable pageable);

    /**
     * 활성화된 대출상품 중 대상주택으로 필터링
     *
     * @param targetHousing 대상주택 키워드
     * @param pageable 페이징 정보
     * @return 필터링된 대출상품 페이지
     */
    Page<LoanProduct> findByActiveTrueAndTargetHousingContaining(String targetHousing, Pageable pageable);

    /**
     * 키워드 검색 (대출이름 또는 대상주택)
     *
     * @param nameKeyword 대출이름 키워드
     * @param housingKeyword 대상주택 키워드
     * @param pageable 페이징 정보
     * @return 검색된 대출상품 페이지
     */
    Page<LoanProduct> findByNameContainingOrTargetHousingContaining(
            String nameKeyword, String housingKeyword, Pageable pageable);

    /**
     * 활성화된 대출상품 중 키워드 검색 (대출이름 또는 대상주택)
     *
     * @param nameKeyword 대출이름 키워드
     * @param housingKeyword 대상주택 키워드
     * @param pageable 페이징 정보
     * @return 검색된 대출상품 페이지
     */
    Page<LoanProduct> findByActiveTrueAndNameContainingOrTargetHousingContaining(
            String nameKeyword, String housingKeyword, Pageable pageable);

    /**
     * ID로 대출상품 조회
     *
     * @param id 대출상품 ID
     * @return 활성화된 대출상품 (Optional)
     */
    Optional<LoanProduct> findById(Long id);

    /**
     * ID로 활성화된 대출상품 조회
     *
     * @param id 대출상품 ID
     * @return 활성화된 대출상품 (Optional)
     */
    Optional<LoanProduct> findByIdAndActiveTrue(Long id);

    /**
     * 활성화된 대출상품 수 조회
     *
     * @return 활성화된 대출상품 수
     */
    long countByActiveTrue();
}
