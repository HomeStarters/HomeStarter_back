package com.dwj.homestarter.calculator.domain;

import com.dwj.homestarter.calculator.dto.external.AssetDto;
import com.dwj.homestarter.calculator.dto.external.HousingDto;
import com.dwj.homestarter.calculator.dto.external.LoanProductDto;
import com.dwj.homestarter.calculator.dto.external.UserProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 계산기 도메인
 * 핵심 재무 계산 로직 및 비즈니스 규칙 캡슐화
 */
@Slf4j
@Component
public class CalculatorDomain {

    /**
     * 입주 후 지출 계산 (가구원 통합 계산 지원)
     *
     * @param dataBundle 외부 데이터 번들 (사용자, 자산, 주택, 대출상품, 가구원 데이터 포함)
     * @param loanAmount 대출 금액
     * @param loanTerm   대출 기간 (개월)
     * @return 계산 결과
     */
    public CalculationResult calculate(ExternalDataBundle dataBundle, Long loanAmount, Integer loanTerm) {
        UserProfileDto user = dataBundle.getUser();
        AssetDto asset = dataBundle.getAsset();
        HousingDto housing = dataBundle.getHousing();
        LoanProductDto loan = dataBundle.getLoan();
        List<ExternalDataBundle.HouseholdMemberData> householdMembers = dataBundle.getHouseholdMembers();

        boolean hasHouseholdMembers = householdMembers != null && !householdMembers.isEmpty();

        // 1. 본인 자산 계산 (isExcludingCalculation=true인 대출액을 totalAssets에 합산)
        long selfExcludedLoanAmount = calculateExcludedLoanAmount(asset.getLoanItems());
        long currentAssets = asset.getTotalAssets() + selfExcludedLoanAmount;

        // 가구원 자산 합산
        if (hasHouseholdMembers) {
            for (ExternalDataBundle.HouseholdMemberData member : householdMembers) {
                currentAssets += (member.getTotalAssets() != null ? member.getTotalAssets() : 0L)
                        + (member.getExcludedLoanAmount() != null ? member.getExcludedLoanAmount() : 0L);
            }
        }

        // 합산 월소득/월지출 계산
        long totalMonthlyIncome = asset.getMonthlyIncome();
        long totalMonthlyExpenses = asset.getMonthlyExpenses();
        if (hasHouseholdMembers) {
            for (ExternalDataBundle.HouseholdMemberData member : householdMembers) {
                totalMonthlyIncome += (member.getTotalMonthlyIncome() != null ? member.getTotalMonthlyIncome() : 0L);
                totalMonthlyExpenses += (member.getTotalMonthlyExpense() != null ? member.getTotalMonthlyExpense() : 0L);
            }
        }

        Long estimatedAssets = calculateEstimatedAssets(housing, currentAssets, totalMonthlyIncome, totalMonthlyExpenses);

        // 2. 대출필요금액 계산
        Long loanRequired = calculateLoanRequired(housing, estimatedAssets);

        // 3. 월 상환액 계산
        Long monthlyPayment = calculateMonthlyPayment(loanAmount, loan.getInterestRate(), loanTerm);

        // 4. LTV 계산
        Double ltv = calculateLTV(loanAmount, housing.getPrice());

        // 5. DTI 계산 (본인 + 가구원 원천징수 소득 합산)
        Long totalAnnualIncome = user.getWithholdingTaxSalary() != null ? user.getWithholdingTaxSalary() : 0L;
        if (hasHouseholdMembers) {
            for (ExternalDataBundle.HouseholdMemberData member : householdMembers) {
                totalAnnualIncome += (member.getWithholdingTaxSalary() != null ? member.getWithholdingTaxSalary() : 0L);
            }
        }
        Long monthlyIncomeForRatio = totalAnnualIncome / 12;
        Double dti = calculateDTI(monthlyPayment, monthlyIncomeForRatio);

        // 6. DSR 계산 (본인 + 가구원의 지출계산 대상 대출에 대한 월 원리금 상환액 합산)
        Long existingLoanPayment = calculateExistingLoanPayment(asset.getLoanItems(), housing.getMoveInDate());
        if (hasHouseholdMembers) {
            for (ExternalDataBundle.HouseholdMemberData member : householdMembers) {
                existingLoanPayment += calculateExistingLoanPayment(member.getLoanItems(), housing.getMoveInDate());
            }
        }
        Double dsr = calculateDSR(monthlyPayment, monthlyIncomeForRatio, existingLoanPayment);

        // 7. 적격성 판단 (지역특성의 LTV/DTI 기준 적용)
        EligibilityResult eligibilityResult = checkEligibility(ltv, dti, dsr, loan, housing, loanAmount);

        // 8. 입주 후 재무상태 계산 (본인 + 가구원 통합)
        AfterMoveInResult afterMoveIn = calculateAfterMoveIn(housing, monthlyPayment,
                estimatedAssets, loanAmount, totalMonthlyIncome, totalMonthlyExpenses);

        return CalculationResult.builder()
                .currentAssets(currentAssets)
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
     * 계산제외 대출액 합산
     *
     * @param loanItems 대출 항목 목록
     * @return 계산제외 대출액 합계
     */
    private long calculateExcludedLoanAmount(List<AssetDto.LoanItemInfo> loanItems) {
        if (loanItems == null) {
            return 0L;
        }
        return loanItems.stream()
                .filter(AssetDto.LoanItemInfo::isExcludingCalculation)
                .mapToLong(loanElement -> loanElement.getAmount() != null ? loanElement.getAmount() : 0L)
                .sum();
    }

    /**
     * 예상자산 계산
     * estimatedAssets = currentAssets + (monthlyIncome - monthlyExpense) × months
     *
     * @param housing              주택 정보
     * @param currentAssets        현재 자산 (본인+가구원 합산)
     * @param totalMonthlyIncome   합산 월소득
     * @param totalMonthlyExpenses 합산 월지출
     * @return 예상자산
     */
    private Long calculateEstimatedAssets(HousingDto housing, long currentAssets,
                                           long totalMonthlyIncome, long totalMonthlyExpenses) {
        long months = ChronoUnit.MONTHS.between(LocalDate.now(), housing.getMoveInDate());
        if (months < 0) {
            months = 0;
        }

        long monthlySavings = totalMonthlyIncome - totalMonthlyExpenses;
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
            if (item.getAmount() == null || item.getAmount() <= 0 || item.isExcludingCalculation()) {
                continue;
            }

            // 잔여 개월 수 계산 (만기일 - 입주 예정일)
            long remainingMonths = ChronoUnit.MONTHS.between(moveInDate, item.getExpirationDate());
            if (remainingMonths <= 0) {
                // 입주 예정일 기준 만기가 지난 대출은 제외
                continue;
            }

            // 대출실행 금액이 있으면 사용, 없으면 대출 잔액 사용
            Long principalAmount = (item.getExecutedAmount() != null && item.getExecutedAmount() > 0)
                    ? item.getExecutedAmount() : item.getAmount();

            // 상환기간이 있으면 사용, 없으면 잔여 개월 수 사용
            int termMonths = (item.getRepaymentPeriod() != null && item.getRepaymentPeriod() > 0)
                    ? item.getRepaymentPeriod() : (int) remainingMonths;

            // 원리금균등상환 방식으로 월 상환액 계산
            log.info("월 상환액 계산 시작 - 대출실행: {}, 금리: {}, 상환기간: {}",
                    principalAmount, item.getInterestRate(), termMonths);
            totalMonthlyPayment += calculateMonthlyPayment(
                    principalAmount,
                    item.getInterestRate(),
                    termMonths
            );
            log.info("월 상환액 총합 : {}", totalMonthlyPayment);
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
     * 입주 후 재무상태 계산 (본인 + 가구원 통합)
     * afterMoveInAssets = estimatedAssets - (housingPrice - loanAmount)
     * afterMoveInExpenses = totalMonthlyExpenses + monthlyPayment
     * availableFunds = totalMonthlyIncome - afterMoveInExpenses
     *
     * @param housing              주택 정보
     * @param monthlyPayment       월 상환액
     * @param estimatedAssets      예상자산 (본인+가구원 합산)
     * @param loanAmount           대출금액
     * @param totalMonthlyIncome   합산 월소득
     * @param totalMonthlyExpenses 합산 월지출
     * @return 입주 후 재무상태
     */
    private AfterMoveInResult calculateAfterMoveIn(HousingDto housing, Long monthlyPayment,
                                                    Long estimatedAssets, Long loanAmount,
                                                    long totalMonthlyIncome, long totalMonthlyExpenses) {
        long afterAssets = estimatedAssets - ((housing.getPrice() - loanAmount) > 0 ? (housing.getPrice() - loanAmount) : 0);
        long afterExpenses = totalMonthlyExpenses + monthlyPayment;
        long availableFunds = totalMonthlyIncome - afterExpenses;

        return AfterMoveInResult.builder()
                .assets(afterAssets)
                .monthlyExpenses(afterExpenses)
                .monthlyIncome(totalMonthlyIncome)
                .availableFunds(availableFunds)
                .build();
    }
}
