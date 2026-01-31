package com.dwj.homestarter.asset.dto.response;

import com.dwj.homestarter.asset.domain.Asset;
import com.dwj.homestarter.asset.dto.AssetItemDto;
import com.dwj.homestarter.asset.dto.ExpenseItemDto;
import com.dwj.homestarter.asset.dto.IncomeItemDto;
import com.dwj.homestarter.asset.dto.LoanItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 자산정보 응답 DTO
 * 자산정보 조회 시 반환되는 데이터 객체
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetResponse {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 결과
     */
    private String result;

    /**
     * 자산 ID
     */
    private String assetId;

    /**
     * 사용자 ID
     */
    private String userId;

    /**
     * 소유자 유형 (SELF/SPOUSE)
     */
    private String ownerType;

    /**
     * 자산 항목 목록
     */
    private List<AssetItemDto> assets;

    /**
     * 대출 항목 목록
     */
    private List<LoanItemDto> loans;

    /**
     * 월소득 항목 목록
     */
    private List<IncomeItemDto> monthlyIncomes;

    /**
     * 월지출 항목 목록
     */
    private List<ExpenseItemDto> monthlyExpenses;

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
     * 생성일시 (yyyy-MM-dd HH:mm:ss)
     */
    private String createdAt;

    /**
     * 수정일시 (yyyy-MM-dd HH:mm:ss)
     */
    private String updatedAt;

    /**
     * Asset 도메인 객체와 항목 목록으로부터 AssetResponse 생성
     *
     * @param asset          Asset 도메인 객체
     * @param assetItems     자산 항목 목록
     * @param loanItems      대출 항목 목록
     * @param incomeItems    월소득 항목 목록
     * @param expenseItems   월지출 항목 목록
     * @return AssetResponse
     */
    public static AssetResponse from(Asset asset,
                                     List<AssetItemDto> assetItems,
                                     List<LoanItemDto> loanItems,
                                     List<IncomeItemDto> incomeItems,
                                     List<ExpenseItemDto> expenseItems,
                                     String result) {
        return AssetResponse.builder()
                .result(result)
                .assetId(asset.getId())
                .userId(asset.getUserId())
                .ownerType(asset.getOwnerType().name())
                .assets(assetItems)
                .loans(loanItems)
                .monthlyIncomes(incomeItems)
                .monthlyExpenses(expenseItems)
                .totalAssets(asset.getTotalAssets())
                .totalLoans(asset.getTotalLoans())
                .totalMonthlyIncome(asset.getTotalMonthlyIncome())
                .totalMonthlyExpense(asset.getTotalMonthlyExpense())
                .netAssets(asset.getNetAssets())
                .monthlyAvailableFunds(asset.getMonthlyAvailableFunds())
                .createdAt(asset.getCreatedAt().format(FORMATTER))
                .updatedAt(asset.getUpdatedAt().format(FORMATTER))
                .build();
    }
}
