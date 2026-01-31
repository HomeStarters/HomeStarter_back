package com.dwj.homestarter.calculator.service;

import com.dwj.homestarter.calculator.dto.request.HousingExpensesRequest;
import com.dwj.homestarter.calculator.dto.response.CalculationResultListResponse;
import com.dwj.homestarter.calculator.dto.response.CalculationResultResponse;
import org.springframework.data.domain.Pageable;

/**
 * 지출 계산 서비스 인터페이스
 * 비즈니스 로직 조율, 트랜잭션 관리, 캐시 전략
 */
public interface ExpenseCalculatorService {

    /**
     * 입주 후 지출 계산
     *
     * @param request 계산 요청
     * @param userId  사용자 ID
     * @return 계산 결과
     */
    CalculationResultResponse calculateHousingExpenses(HousingExpensesRequest request, String userId);

    /**
     * 계산 결과 목록 조회
     *
     * @param userId    사용자 ID
     * @param housingId 주택 ID (optional)
     * @param status    상태 (optional)
     * @param pageable  페이징 정보
     * @return 계산 결과 목록
     */
    CalculationResultListResponse getCalculationResults(String userId, String housingId,
                                                         String status, Pageable pageable);

    /**
     * 계산 결과 상세 조회
     *
     * @param id     계산 결과 ID
     * @param userId 사용자 ID
     * @return 계산 결과 상세
     */
    CalculationResultResponse getCalculationResult(String id, String userId);

    /**
     * 계산 결과 삭제
     *
     * @param id     계산 결과 ID
     * @param userId 사용자 ID
     */
    void deleteCalculationResult(String id, String userId);
}
