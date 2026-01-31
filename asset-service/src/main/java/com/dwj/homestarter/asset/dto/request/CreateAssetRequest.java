package com.dwj.homestarter.asset.dto.request;

import com.dwj.homestarter.asset.dto.AssetItemDto;
import com.dwj.homestarter.asset.dto.ExpenseItemDto;
import com.dwj.homestarter.asset.dto.IncomeItemDto;
import com.dwj.homestarter.asset.dto.LoanItemDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 자산정보 생성 요청 DTO
 * 본인/배우자 자산정보 생성 시 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAssetRequest {

    /**
     * 자산 항목 목록
     */
    @Valid
    @Builder.Default
    private List<AssetItemDto> assets = new ArrayList<>();

    /**
     * 대출 항목 목록
     */
    @Valid
    @Builder.Default
    private List<LoanItemDto> loans = new ArrayList<>();

    /**
     * 월소득 항목 목록
     */
    @Valid
    @Builder.Default
    private List<IncomeItemDto> monthlyIncomes = new ArrayList<>();

    /**
     * 월지출 항목 목록
     */
    @Valid
    @Builder.Default
    private List<ExpenseItemDto> monthlyExpenses = new ArrayList<>();
}
