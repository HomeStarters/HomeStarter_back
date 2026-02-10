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

        // 1. 예상자산 계산 (isExcludingCalculation=true인 대출액을 totalAssets에 합산)
        Long estimatedAssets = calculateEstimatedAssets(asset, housing);

        // 2. 대출필요금액 계산
        Long loanRequired = calculateLoanRequired(housing, estimatedAssets);

        // 3. 월 상환액 계산
        Long monthlyPayment = calculateMonthlyPayment(loanAmount, loan.getInterestRate(), loanTerm);

        // 4. LTV 계산
        Double ltv = calculateLTV(loanAmount, housing.getPrice());
//        Double ltv = calculateLTV(loanRequired, housing.getPrice());

        // 5. DTI 계산 (원천징수 소득 기준)
        Long annualIncomeForRatio = user.getWithholdingTaxSalary();
        Long monthlyIncomeForRatio = annualIncomeForRatio != null ? annualIncomeForRatio / 12 : 0L;
        Double dti = calculateDTI(monthlyPayment, monthlyIncomeForRatio);

        // 6. DSR 계산 (원천징수 소득 기준, 입주 예정일 기준으로 기존 대출 잔여 상환액 계산)
        Long existingLoanPayment = calculateExistingLoanPayment(asset.getLoanItems(), housing.getMoveInDate());
        Double dsr = calculateDSR(monthlyPayment, monthlyIncomeForRatio, existingLoanPayment);

        // 7. 적격성 판단 (지역특성의 LTV/DTI 기준 적용)
        EligibilityResult eligibilityResult = checkEligibility(ltv, dti, dsr, loan, housing, loanAmount);

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
     * estimatedAssets = currentAssets + excludedLoanAmount + (monthlyIncome - monthlyExpense) × months
     * - isExcludingCalculation=true인 대출액은 계산에서 제외되어야 하므로 totalAssets에 다시 합산
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

        // isExcludingCalculation=true인 대출액 합산 (계산에서 제외해야 하는 대출이므로 자산에 다시 더함)
        long excludedLoanAmount = 0L;
        if (asset.getLoanItems() != null) {
            excludedLoanAmount = asset.getLoanItems().stream()
                    .filter(AssetDto.LoanItemInfo::isExcludingCalculation)
                    .mapToLong(loan -> loan.getAmount() != null ? loan.getAmount() : 0L)
                    .sum();
        }

        long currentAssets = asset.getTotalAssets() + excludedLoanAmount;
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
     * 기존 대출 월 상환액 계산
     * 각 대출 항목의 금리와 만기일을 기반으로 원리금균등상환 방식으로 월 상환액을 계산
     * 잔여 개월 수는 입주 예정일 기준으로 산정
     *
     * @param loanItems  개별 대출 항목 목록
     * @param moveInDate 입주 예정일
     * @return 기존 대출 총 월 상환액
     */
    private Long calculateExistingLoanPayment(List<AssetDto.LoanItemInfo> loanItems, LocalDate moveInDate) {
        if (loanItems == null || loanItems.isEmpty()) {
            return 0L;
        }

        long totalMonthlyPayment = 0L;

        for (AssetDto.LoanItemInfo item : loanItems) {
            if (item.getAmount() == null || item.getAmount() <= 0) {
                continue;
            }

            // 잔여 개월 수 계산 (만기일 - 입주 예정일)
            long remainingMonths = ChronoUnit.MONTHS.between(moveInDate, item.getExpirationDate());
            if (remainingMonths <= 0) {
                // 입주 예정일 기준 만기가 지난 대출은 제외
                continue;
            }

            // 원리금균등상환 방식으로 월 상환액 계산
            totalMonthlyPayment += calculateMonthlyPayment(
                    item.getAmount(),
                    item.getInterestRate(),
                    (int) remainingMonths
            );
        }

        return totalMonthlyPayment;
    }

    /**
     * 적격성 판단
     * isEligible = (LTV ≤ ltvLimit) AND (DTI ≤ dtiLimit) AND (DSR ≤ dsrLimit) AND (loanRequired ≤ maxAmount)
     * - LTV/DTI 한도는 주택의 지역특성(regionalCharacteristic)에서 가져옴
     * - DSR 한도는 대출상품에서 가져옴
     *
     * @param ltv          계산된 LTV
     * @param dti          계산된 DTI
     * @param dsr          계산된 DSR
     * @param loan         대출상품 정보
     * @param housing      주택 정보 (지역특성 포함)
     * @param loanAmount   대출금액
     * @return 적격성 결과
     */
    private EligibilityResult checkEligibility(Double ltv, Double dti, Double dsr,
                                                LoanProductDto loan, HousingDto housing, Long loanAmount) {
        List<String> reasons = new ArrayList<>();
        boolean isEligible = true;

        // 지역특성에서 LTV/DTI 한도 가져오기 (비율 → 백분율 변환)
        Double ltvLimit = null;
        Double dtiLimit = null;
        if (housing.getRegionalCharacteristic() != null) {
            if (housing.getRegionalCharacteristic().getLtv() != null) {
                ltvLimit = housing.getRegionalCharacteristic().getLtv() * 100;
            }
            if (housing.getRegionalCharacteristic().getDti() != null) {
                dtiLimit = housing.getRegionalCharacteristic().getDti() * 100;
            }
        }

        // LTV 검증: 대출상품 적용여부 + 지역특성 한도
        if (Boolean.TRUE.equals(loan.getIsApplyLtv())) {
            if (ltvLimit != null && ltv > ltvLimit) {
                isEligible = false;
                reasons.add(String.format("LTV 초과 (%.2f%% > %.2f%%)", ltv, ltvLimit));
            }
        }

        // DTI 검증: 대출상품 적용여부 + 지역특성 한도
        if (Boolean.TRUE.equals(loan.getIsApplyDti())) {
            if (dti == 0.0) {
                isEligible = false;
                reasons.add("원천징수 소득 없음 (프로필 입력 페이지에서 입력 필요)");
            }
            else if (dtiLimit != null && dti > dtiLimit) {
                isEligible = false;
                reasons.add(String.format("DTI 초과 (%.2f%% > %.2f%%)", dti, dtiLimit));
            }
        }

        // DSR 검증: 대출상품 한도 기준
        if (Boolean.TRUE.equals(loan.getIsApplyDsr())) {
            if (dsr == 0.0) {
                isEligible = false;
                reasons.add("원천징수 소득 없음 (프로필 입력 페이지에서 입력 필요)");
            }
            else if (loan.getDsrLimit() != null && dsr > loan.getDsrLimit()) {
                isEligible = false;
                reasons.add(String.format("DSR 초과 (%.2f%% > %.2f%%)", dsr, loan.getDsrLimit()));
            }
        }

        // 최대 대출금액 검증
        if (loan.getMaxAmount() != null && loanAmount > loan.getMaxAmount()) {
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
