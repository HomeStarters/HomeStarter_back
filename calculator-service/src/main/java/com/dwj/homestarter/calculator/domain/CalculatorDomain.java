package com.dwj.homestarter.calculator.domain;

import com.dwj.homestarter.calculator.dto.external.AssetDto;
import com.dwj.homestarter.calculator.dto.external.HousingDto;
import com.dwj.homestarter.calculator.dto.external.LoanProductDto;
import com.dwj.homestarter.calculator.dto.external.UserProfileDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 계산기 도메인
 * 핵심 재무 계산 로직 및 비즈니스 규칙 캡슐화
 */
@Component
public class CalculatorDomain {

    /**
     * 입주 후 지출 계산
     *
     * @param user        사용자 프로필
     * @param asset       자산 정보
     * @param housing     주택 정보
     * @param loan        대출상품 정보
     * @param loanAmount  대출 금액
     * @param loanTerm    대출 기간 (개월)
     * @return 계산 결과
     */
    public CalculationResult calculate(UserProfileDto user, AssetDto asset, HousingDto housing,
                                        LoanProductDto loan, Long loanAmount, Integer loanTerm) {

        // 1. 예상자산 계산
        Long estimatedAssets = calculateEstimatedAssets(asset, housing);

        // 2. 대출필요금액 계산
        Long loanRequired = calculateLoanRequired(housing, estimatedAssets);

        // 3. 월 상환액 계산
        Long monthlyPayment = calculateMonthlyPayment(loanAmount, loan.getInterestRate(), loanTerm);

        // 4. LTV 계산
        Double ltv = calculateLTV(loanAmount, housing.getPrice());
//        Double ltv = calculateLTV(loanRequired, housing.getPrice());

        // 5. DTI 계산
        Double dti = calculateDTI(monthlyPayment, asset.getMonthlyIncome());

        // 6. DSR 계산
        Long existingLoanPayment = calculateExistingLoanPayment(asset.getTotalLoans());
        Double dsr = calculateDSR(monthlyPayment, asset.getMonthlyIncome(), existingLoanPayment);

        // 7. 적격성 판단
        EligibilityResult eligibilityResult = checkEligibility(ltv, dti, dsr, loan, loanAmount);

        // 8. 입주 후 재무상태 계산
        AfterMoveInResult afterMoveIn = calculateAfterMoveIn(asset, housing, monthlyPayment,
                estimatedAssets, loanAmount);

        return CalculationResult.builder()
                .estimatedAssets(estimatedAssets)
                .loanRequired(loanRequired)
                .ltv(ltv)
                .dti(dti)
                .dsr(dsr)
                .isEligible(eligibilityResult.getIsEligible())
                .ineligibilityReasons(eligibilityResult.getReasons())
                .monthlyPayment(monthlyPayment)
                .afterMoveInAssets(afterMoveIn.getAssets())
                .afterMoveInMonthlyExpenses(afterMoveIn.getMonthlyExpenses())
                .afterMoveInMonthlyIncome(afterMoveIn.getMonthlyIncome())
                .afterMoveInAvailableFunds(afterMoveIn.getAvailableFunds())
                .build();
    }

    /**
     * 예상자산 계산
     * estimatedAssets = currentAssets + (monthlyIncome - monthlyExpense) × months - totalLoans
     *
     * @param asset   자산 정보
     * @param housing 주택 정보
     * @return 예상자산
     */
    private Long calculateEstimatedAssets(AssetDto asset, HousingDto housing) {
        long months = ChronoUnit.MONTHS.between(LocalDate.now(), housing.getMoveInDate());
        if (months < 0) {
            months = 0;
        }

        long monthlySavings = asset.getMonthlyIncome() - asset.getMonthlyExpenses();
        long currentAssets = asset.getTotalAssets();
//        long currentAssets = asset.getTotalAssets() - asset.getTotalLoans();

        return currentAssets + (monthlySavings * months);
    }

    /**
     * 대출필요금액 계산
     * loanRequired = housingPrice - estimatedAssets
     *
     * @param housing          주택 정보
     * @param estimatedAssets  예상자산
     * @return 대출필요금액
     */
    private Long calculateLoanRequired(HousingDto housing, Long estimatedAssets) {
        long required = housing.getPrice() - estimatedAssets;
        return Math.max(required, 0L);
    }

    /**
     * LTV 계산 (Loan To Value)
     * LTV = (loanRequired / housingPrice) × 100
     *
     * @param loanRequired 대출필요금액
     * @param housingPrice 주택 가격
     * @return LTV (%)
     */
    private Double calculateLTV(Long loanRequired, Long housingPrice) {
        if (housingPrice == 0) {
            return 0.0;
        }
        return (loanRequired.doubleValue() / housingPrice.doubleValue()) * 100;
    }

    /**
     * DTI 계산 (Debt To Income)
     * DTI = (연간대출원리금 / 연소득) × 100
     *
     * @param monthlyPayment 월 상환액
     * @param monthlyIncome  월 소득
     * @return DTI (%)
     */
    private Double calculateDTI(Long monthlyPayment, Long monthlyIncome) {
        if (monthlyIncome == 0) {
            return 0.0;
        }
        long annualPayment = monthlyPayment * 12;
        long annualIncome = monthlyIncome * 12;
        return ((double) annualPayment / (double) annualIncome) * 100;
    }

    /**
     * DSR 계산 (Debt Service Ratio)
     * DSR = (연간총부채원리금 / 연소득) × 100
     *
     * @param monthlyPayment        월 상환액
     * @param monthlyIncome         월 소득
     * @param existingLoanPayment   기존 대출 월 상환액
     * @return DSR (%)
     */
    private Double calculateDSR(Long monthlyPayment, Long monthlyIncome, Long existingLoanPayment) {
        if (monthlyIncome == 0) {
            return 0.0;
        }
        long totalMonthlyPayment = monthlyPayment + existingLoanPayment;
        long annualTotalPayment = totalMonthlyPayment * 12;
        long annualIncome = monthlyIncome * 12;
        return ((double) annualTotalPayment / (double) annualIncome) * 100;
    }

    /**
     * 월 상환액 계산 (원리금균등상환)
     * monthlyPayment = loanAmount × (monthlyRate × (1 + monthlyRate)^months) / ((1 + monthlyRate)^months - 1)
     *
     * @param loanAmount   대출 금액
     * @param interestRate 연 이자율 (%)
     * @param loanTerm     대출 기간 (개월)
     * @return 월 상환액
     */
    private Long calculateMonthlyPayment(Long loanAmount, Double interestRate, Integer loanTerm) {
        if (loanAmount == 0 || loanTerm == 0) {
            return 0L;
        }

        double monthlyRate = interestRate / 100 / 12;
        if (monthlyRate == 0) {
            return loanAmount / loanTerm;
        }

        double temp = Math.pow(1 + monthlyRate, loanTerm);
        double monthlyPayment = loanAmount * (monthlyRate * temp) / (temp - 1);

        return Math.round(monthlyPayment);
    }

    /**
     * 기존 대출 월 상환액 추정
     * 단순화: 총 대출액을 20년(240개월) 균등 분할
     *
     * @param totalLoans 총 대출액
     * @return 기존 대출 월 상환액
     */
    private Long calculateExistingLoanPayment(Long totalLoans) {
        if (totalLoans == 0) {
            return 0L;
        }
        // 평균 대출 기간 20년(240개월) 가정
        return totalLoans / 240;
    }

    /**
     * 적격성 판단
     * isEligible = (LTV ≤ ltvLimit) AND (DTI ≤ dtiLimit) AND (DSR ≤ dsrLimit) AND (loanRequired ≤ maxAmount)
     *
     * @param ltv          계산된 LTV
     * @param dti          계산된 DTI
     * @param dsr          계산된 DSR
     * @param loan         대출상품 정보
     * @param loanAmount 대출필요금액
     * @return 적격성 결과
     */
    private EligibilityResult checkEligibility(Double ltv, Double dti, Double dsr,
                                                LoanProductDto loan, Long loanAmount) {
        List<String> reasons = new ArrayList<>();
        boolean isEligible = true;

        if (ltv > loan.getLtvLimit()) {
            isEligible = false;
            reasons.add(String.format("LTV 초과 (%.2f%% > %.2f%%)", ltv, loan.getLtvLimit()));
        }

        if (dti > loan.getDtiLimit()) {
            isEligible = false;
            reasons.add(String.format("DTI 초과 (%.2f%% > %.2f%%)", dti, loan.getDtiLimit()));
        }

        if (dsr > loan.getDsrLimit()) {
            isEligible = false;
            reasons.add(String.format("DSR 초과 (%.2f%% > %.2f%%)", dsr, loan.getDsrLimit()));
        }

        if (loanAmount > loan.getMaxAmount()) {
            isEligible = false;
            reasons.add(String.format("최대 대출 금액 초과 (%,d원 > %,d원)", loanAmount, loan.getMaxAmount()));
        }

        return EligibilityResult.builder()
                .isEligible(isEligible)
                .reasons(reasons)
                .build();
    }

    /**
     * 입주 후 재무상태 계산
     * afterMoveInAssets = estimatedAssets - loanRequired
     * afterMoveInExpenses = monthlyExpense + monthlyPayment
     * availableFunds = monthlyIncome - afterMoveInExpenses
     *
     * @param asset           자산 정보
     * @param housing         주택 정보
     * @param monthlyPayment  월 상환액
     * @param estimatedAssets 예상자산
     * @param loanAmount    대출필요금액
     * @return 입주 후 재무상태
     */
    private AfterMoveInResult calculateAfterMoveIn(AssetDto asset, HousingDto housing,
                                                    Long monthlyPayment, Long estimatedAssets,
                                                    Long loanAmount) {
        long afterAssets = estimatedAssets - (housing.getPrice() - loanAmount);
        long afterExpenses = asset.getMonthlyExpenses() + monthlyPayment;
        long availableFunds = asset.getMonthlyIncome() - afterExpenses;

        return AfterMoveInResult.builder()
                .assets(afterAssets)
                .monthlyExpenses(afterExpenses)
                .monthlyIncome(asset.getMonthlyIncome())
                .availableFunds(availableFunds)
                .build();
    }
}
