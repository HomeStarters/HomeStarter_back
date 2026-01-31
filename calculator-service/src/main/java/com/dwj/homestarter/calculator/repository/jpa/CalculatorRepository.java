package com.dwj.homestarter.calculator.repository.jpa;

import com.dwj.homestarter.calculator.repository.entity.CalculationResultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 계산 결과 리포지토리
 * 데이터 영속성 및 CRUD 작업 처리
 */
@Repository
public interface CalculatorRepository extends JpaRepository<CalculationResultEntity, String> {

    /**
     * 사용자 ID로 계산 결과 목록 조회 (페이징)
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 계산 결과 목록
     */
    Page<CalculationResultEntity> findByUserId(String userId, Pageable pageable);

    /**
     * 사용자 ID와 주택 ID로 계산 결과 목록 조회 (페이징)
     *
     * @param userId    사용자 ID
     * @param housingId 주택 ID
     * @param pageable  페이징 정보
     * @return 계산 결과 목록
     */
    Page<CalculationResultEntity> findByUserIdAndHousingId(String userId, String housingId, Pageable pageable);

    /**
     * 사용자 ID와 상태로 계산 결과 목록 조회 (페이징)
     *
     * @param userId   사용자 ID
     * @param status   상태 (ELIGIBLE, INELIGIBLE)
     * @param pageable 페이징 정보
     * @return 계산 결과 목록
     */
    Page<CalculationResultEntity> findByUserIdAndStatus(String userId, String status, Pageable pageable);

    /**
     * ID와 사용자 ID로 계산 결과 조회 (권한 확인용)
     *
     * @param id     계산 결과 ID
     * @param userId 사용자 ID
     * @return 계산 결과
     */
    Optional<CalculationResultEntity> findByIdAndUserId(String id, String userId);

    /**
     * 주택 ID로 영향받는 사용자 ID 목록 조회 (캐시 무효화용)
     *
     * @param housingId 주택 ID
     * @return 사용자 ID 목록
     */
    @Query("SELECT DISTINCT c.userId FROM CalculationResultEntity c WHERE c.housingId = :housingId")
    List<String> findUserIdsByHousingId(@Param("housingId") String housingId);
}
