package com.dwj.homestarter.asset.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 자산정보 도메인 모델
 * 본인/배우자별 자산 정보를 표현하는 핵심 도메인 객체
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {

    /**
     * 자산정보 ID
     */
    private String id;

    /**
     * 사용자 ID
     */
    private String userId;

    /**
     * 소유자 유형 (SELF/SPOUSE)
     */
    private OwnerType ownerType;

    /**
     * 총 자산액 (원)
     */
    private Long totalAssets;

    /**
     * 총 대출액 (원)
     */
    private Long totalLoans;

    /**
     * 총 월소득 (원)
     */
    private Long totalMonthlyIncome;

    /**
     * 총 월지출 (원)
     */
    private Long totalMonthlyExpense;

    /**
     * 순자산 (원)
     */
    private Long netAssets;

    /**
     * 월 가용자금 (원)
     */
    private Long monthlyAvailableFunds;

    /**
     * 생성일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    private LocalDateTime updatedAt;

    /**
     * 순자산 계산 (총 자산 - 총 대출)
     *
     * @return 순자산 금액
     */
    public Long calculateNetAssets() {
        return totalAssets - totalLoans;
    }

    /**
     * 월 가용자금 계산 (총 월소득 - 총 월지출)
     *
     * @return 월 가용자금
     */
    public Long calculateMonthlyAvailableFunds() {
        return totalMonthlyIncome - totalMonthlyExpense;
    }

    /**
     * 총액 업데이트
     * 자산 항목들의 합계를 기반으로 총액 필드 업데이트
     *
     * @param totalAssets          총 자산액
     * @param totalLoans           총 대출액
     * @param totalMonthlyIncome   총 월소득
     * @param totalMonthlyExpense  총 월지출
     */
    public void updateTotals(Long totalAssets, Long totalLoans, Long totalMonthlyIncome, Long totalMonthlyExpense) {
        this.totalAssets = totalAssets;
        this.totalLoans = totalLoans;
        this.totalMonthlyIncome = totalMonthlyIncome;
        this.totalMonthlyExpense = totalMonthlyExpense;
        this.netAssets = calculateNetAssets();
        this.monthlyAvailableFunds = calculateMonthlyAvailableFunds();
    }
}
