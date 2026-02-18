package com.dwj.homestarter.calculator.domain;

import com.dwj.homestarter.calculator.dto.external.AssetDto;
import com.dwj.homestarter.calculator.dto.external.HousingDto;
import com.dwj.homestarter.calculator.dto.external.LoanProductDto;
import com.dwj.homestarter.calculator.dto.external.UserProfileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 외부 서비스 데이터 번들
 * 외부 서비스로부터 수집한 데이터를 담는 컨테이너
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalDataBundle {

    /**
     * 사용자 프로필 정보
     */
    private UserProfileDto user;

    /**
     * 자산 정보
     */
    private AssetDto asset;

    /**
     * 주택 정보
     */
    private HousingDto housing;

    /**
     * 대출상품 정보
     */
    private LoanProductDto loan;

    /**
     * 지출 계산에 포함된 가구원 데이터 목록
     */
    private List<HouseholdMemberData> householdMembers;

    /**
     * 가구원 데이터
     * 각 가구원의 프로필 및 자산 정보를 담는 컨테이너
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HouseholdMemberData {
        /**
         * 가구원 사용자 ID
         */
        private String userId;

        /**
         * 가구원 이름
         */
        private String userName;

        /**
         * 원천징수 소득 (원/년)
         */
        private Long withholdingTaxSalary;

        /**
         * 총 자산 (원)
         */
        private Long totalAssets;

        /**
         * 총 대출 (원)
         */
        private Long totalLoans;

        /**
         * 월 소득 (원)
         */
        private Long totalMonthlyIncome;

        /**
         * 월 지출 (원)
         */
        private Long totalMonthlyExpense;

        /**
         * 계산제외 대출액 합계 (원)
         */
        private Long excludedLoanAmount;

        /**
         * 개별 대출 항목 목록 (DSR 계산용)
         */
        private List<AssetDto.LoanItemInfo> loanItems;
    }
}
