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
     * 단일 대출의 월 상환액 계산
     * 대출 유형과 상환 방식에 따라 연 원리금상환액을 계산하고 12로 나누어 월 상환액 반환
     *
     * @param loanType          대출 유형 (null이면 OTHER)
     * @param repaymentType     상환 방식 (null이면 EPI)
     * @param loanAmount        대출 원금
     * @param annualInterestRate 연 이자율 (%)
     * @param repaymentPeriod   상환기간 (개월)
     * @param gracePeriod       거치기간 (개월, null이면 0)
     * @return 월 상환액
     */
    public Long calculateLoanMonthlyPayment(LoanType loanType, RepaymentType repaymentType,
                                             Long loanAmount, Double annualInterestRate,
                                             Integer repaymentPeriod, Integer gracePeriod) {
        if (loanAmount == null || loanAmount <= 0 || repaymentPeriod == null || repaymentPeriod <= 0) {
            return 0L;
        }

        LoanType effectiveLoanType = loanType != null ? loanType : LoanType.OTHER;
        RepaymentType effectiveRepaymentType = repaymentType != null ? repaymentType : RepaymentType.EPI;
        int gracePeriodMonths = gracePeriod != null ? gracePeriod : 0;

        long annualPayment = calculateAnnualPaymentByType(
                loanAmount, annualInterestRate, repaymentPeriod, gracePeriodMonths,
                effectiveLoanType, effectiveRepaymentType, null);

        return annualPayment > 0 ? Math.round(annualPayment / 12.0) : 0L;
    }

    /**
     * 입주 후 지출 계산 (가구원 통합 계산 지원)
     *
     * @param dataBundle                외부 데이터 번들 (사용자, 자산, 주택, 대출상품, 가구원 데이터 포함)
     * @param loanAmount                대출 금액
     * @param loanTerm                  대출 기간 (개월)
     * @param useLoanRequiredAsLoanAmount 대출필요금액을 대출금액으로 사용 여부
     * @param repaymentType             신규 대출 상환 방식 (null이면 EPI)
     * @param gracePeriod               신규 대출 거치기간 (개월, null이면 0)
     * @return 계산 결과
     */
    public CalculationResult calculate(ExternalDataBundle dataBundle, Long loanAmount, Integer loanTerm,
                                       Boolean useLoanRequiredAsLoanAmount,
                                       RepaymentType repaymentType, Integer gracePeriod) {
        // loanAmount가 null인 경우 0으로 처리 (useLoanRequiredAsLoanAmount=true일 때 미입력 가능)
        if (loanAmount == null) {
            loanAmount = 0L;
        }

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

        // 2-1. 대출필요금액을 대출금액으로 사용하는 경우 대체
        Long effectiveLoanAmount = loanAmount;
        if (Boolean.TRUE.equals(useLoanRequiredAsLoanAmount)) {
            effectiveLoanAmount = loanRequired;
            log.info("대출필요금액을 대출금액으로 사용 - loanRequired: {}", loanRequired);
        }

        // 3. 신규 대출 상환방식별 연 원리금상환액 및 월 상환액 계산
        RepaymentType effectiveRepaymentType = (repaymentType != null) ? repaymentType : RepaymentType.EPI;
        int effectiveGracePeriod = (gracePeriod != null) ? gracePeriod : 0;

        Long newLoanAnnualPayment = calculateMortgageAnnualPayment(
                effectiveLoanAmount, loan.getInterestRate(), loanTerm, effectiveGracePeriod, effectiveRepaymentType, null);
        // 월 상환액 = 연 원리금상환액 / 12
        Long monthlyPayment = (newLoanAnnualPayment > 0) ? Math.round(newLoanAnnualPayment / 12.0) : 0L;

        log.info("신규 대출 계산 - 상환방식: {}, 거치기간: {}개월, 연 원리금: {}, 월 상환액: {}",
                effectiveRepaymentType, effectiveGracePeriod, newLoanAnnualPayment, monthlyPayment);

        // 4. LTV 계산
        Double ltv = calculateLTV(effectiveLoanAmount, housing.getPrice());

        // 5. DTI 계산 (본인 + 가구원 원천징수 소득 합산)
        Long totalAnnualIncome = user.getWithholdingTaxSalary() != null ? user.getWithholdingTaxSalary() : 0L;
        if (hasHouseholdMembers) {
            for (ExternalDataBundle.HouseholdMemberData member : householdMembers) {
                totalAnnualIncome += (member.getWithholdingTaxSalary() != null ? member.getWithholdingTaxSalary() : 0L);
            }
        }
        Long monthlyIncomeForRatio = totalAnnualIncome / 12;
        Double dti = calculateDTI(monthlyPayment, monthlyIncomeForRatio);

        // 6. DSR 계산 (대출유형/상환방식별 연 원리금상환액 합산 방식)
        // 기존 대출 연 원리금상환액 (대출유형/상환방식별 개별 계산)
        Long existingAnnualPayment = calculateExistingLoanAnnualPayment(asset.getLoanItems(), housing.getMoveInDate());
        if (hasHouseholdMembers) {
            for (ExternalDataBundle.HouseholdMemberData member : householdMembers) {
                existingAnnualPayment += calculateExistingLoanAnnualPayment(member.getLoanItems(), housing.getMoveInDate());
            }
        }
        Double dsr = calculateDSR(newLoanAnnualPayment, totalAnnualIncome, existingAnnualPayment);

        // 7. 적격성 판단 (지역특성의 LTV/DTI 기준 적용)
        EligibilityResult eligibilityResult = checkEligibility(ltv, dti, dsr, loan, housing, effectiveLoanAmount);

        // 8. 입주 후 재무상태 계산 (본인 + 가구원 통합)
        AfterMoveInResult afterMoveIn = calculateAfterMoveIn(housing, monthlyPayment,
                estimatedAssets, effectiveLoanAmount, totalMonthlyIncome, totalMonthlyExpenses);

        return CalculationResult.builder()
                .effectiveLoanAmount(effectiveLoanAmount)
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
     * 기존 대출의 연 원리금상환액은 대출유형/상환방식별로 개별 계산하여 합산
     *
     * @param newLoanAnnualPayment  신규 대출 연 원리금상환액
     * @param annualIncome          연소득
     * @param existingAnnualPayment 기존 대출 연 원리금상환액 합계
     * @return DSR (%)
     */
    private Double calculateDSR(Long newLoanAnnualPayment, Long annualIncome, Long existingAnnualPayment) {
        if (annualIncome == 0) {
            return 0.0;
        }
        long totalAnnualPayment = newLoanAnnualPayment + existingAnnualPayment;
        return ((double) totalAnnualPayment / (double) annualIncome) * 100;
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
     * 기존 대출의 연 원리금상환액 계산 (대출유형/상환방식별 DSR 계산)
     * 각 대출 항목의 유형과 상환방식에 따라 연 원리금상환액을 개별 계산하여 합산
     *
     * @param loanItems  개별 대출 항목 목록
     * @param moveInDate 입주 예정일
     * @return 기존 대출 총 연 원리금상환액
     */
    private Long calculateExistingLoanAnnualPayment(List<AssetDto.LoanItemInfo> loanItems, LocalDate moveInDate) {
        if (loanItems == null || loanItems.isEmpty()) {
            return 0L;
        }

        long totalAnnualPayment = 0L;

        for (AssetDto.LoanItemInfo item : loanItems) {
            if (item.getAmount() == null || item.getAmount() <= 0 || item.isExcludingCalculation()) {
                continue;
            }

            // 잔여 개월 수 계산 (만기일 - 입주 예정일)
            long remainingMonths = ChronoUnit.MONTHS.between(moveInDate, item.getExpirationDate());
            if (remainingMonths <= 0) {
                continue;
            }

            // 대출실행 금액이 있으면 사용, 없으면 대출 잔액 사용
            Long principalAmount = (item.getExecutedAmount() != null && item.getExecutedAmount() > 0)
                    ? item.getExecutedAmount() : item.getAmount();

            // 상환기간이 있으면 사용, 없으면 잔여 개월 수 사용
            int termMonths = (item.getRepaymentPeriod() != null && item.getRepaymentPeriod() > 0)
                    ? item.getRepaymentPeriod() : (int) remainingMonths;

            int gracePeriodMonths = (item.getGracePeriod() != null) ? item.getGracePeriod() : 0;
            LoanType loanType = (item.getLoanType() != null) ? item.getLoanType() : LoanType.OTHER;
            RepaymentType repaymentType = (item.getRepaymentType() != null) ? item.getRepaymentType() : RepaymentType.EPI;

            long annualPayment = calculateAnnualPaymentByType(
                    principalAmount, item.getInterestRate(), termMonths, gracePeriodMonths,
                    loanType, repaymentType, item.getExpirationDate());

            log.info("연 원리금상환액 계산 - 대출유형: {}, 상환방식: {}, 원금: {}, 금리: {}, 상환기간: {}개월, 거치기간: {}개월, 연상환액: {}",
                    loanType, repaymentType, principalAmount, item.getInterestRate(), termMonths, gracePeriodMonths, annualPayment);

            totalAnnualPayment += annualPayment;
        }

        log.info("기존 대출 총 연 원리금상환액: {}", totalAnnualPayment);
        return totalAnnualPayment;
    }

    /**
     * 대출유형별 연 원리금상환액 계산 분기
     *
     * @param principal         대출 원금
     * @param interestRate      연 이자율 (%)
     * @param termMonths        상환기간 (개월)
     * @param gracePeriodMonths 거치기간 (개월)
     * @param loanType          대출 유형
     * @param repaymentType     상환 방식
     * @param expirationDate    만기일 (기존 대출 스케줄 계산용, 신규 대출은 null)
     * @return 연 원리금상환액
     */
    private long calculateAnnualPaymentByType(Long principal, Double interestRate, int termMonths,
                                               int gracePeriodMonths, LoanType loanType, RepaymentType repaymentType,
                                               LocalDate expirationDate) {
        switch (loanType) {
            case JEONSE:
                // 전세대출: 원금 제외, 연 이자만 계산
                return calculateJeonseAnnualPayment(principal, interestRate);

            case CREDIT:
            case OTHER:
                // 신용대출/기타대출: 원리금균등, 상환기간 치환 시 원금/이자 분리 계산
                return calculateCreditAnnualPayment(principal, interestRate, termMonths, expirationDate);

            case MORTGAGE:
            default:
                // 주택담보대출: 상환방식별 분기
                return calculateMortgageAnnualPayment(principal, interestRate, termMonths,
                        gracePeriodMonths, repaymentType, expirationDate);
        }
    }

    /**
     * 전세대출 연 원리금상환액 계산
     * 원금 제외, 연 이자만 = 원금 × 연이자율
     */
    private long calculateJeonseAnnualPayment(Long principal, Double interestRate) {
        if (principal == 0 || interestRate == 0) {
            return 0L;
        }
        return Math.round(principal * interestRate / 100.0);
    }

    /**
     * 신용대출/기타대출 연 원리금상환액 계산
     * 원리금균등 상환, 상환기간 5년(60개월) 이하면 5년 고정, 10년(120개월) 이상이면 10년 고정.
     *
     * <p>상환기간 치환이 발생한 경우 (기존 대출에서 expirationDate 존재 시):
     * <ul>
     *   <li>연 원금 상환액: 치환된 상환기간 기준 (원금 / 치환 기간(년))</li>
     *   <li>연 이자 상환액: 원래 상환기간의 EPI 스케줄에서 현재 회차부터 12개월 이자 합산</li>
     * </ul>
     *
     * @param principal      대출 원금
     * @param interestRate   연 이자율 (%)
     * @param termMonths     원래 상환기간 (개월)
     * @param expirationDate 만기일 (기존 대출 스케줄 계산용, 신규 대출은 null)
     * @return 연 원리금상환액
     */
    private long calculateCreditAnnualPayment(Long principal, Double interestRate, int termMonths,
                                               LocalDate expirationDate) {
        int effectiveTermMonths;
        if (termMonths <= 60) {
            effectiveTermMonths = 60; // 5년 이하 → 5년(60개월) 고정
        } else if (termMonths >= 120) {
            effectiveTermMonths = 120; // 10년 이상 → 10년(120개월) 고정
        } else {
            // 치환 없음: 기존 로직 그대로
            Long monthlyPayment = calculateMonthlyPayment(principal, interestRate, termMonths);
            return monthlyPayment * 12;
        }

        // ── 치환 발생 케이스 ────────────────────────────────────────────────
        log.info("신용대출 상환기간 치환 - 원래: {}개월 → 치환: {}개월 (원금/이자 분리 계산)",
                termMonths, effectiveTermMonths);

        // 연 원금 상환액: 치환된 기간 기준 (원금 / 치환 기간(년))
        double effectiveTermYears = effectiveTermMonths / 12.0;
        long annualPrincipalPayment = Math.round((double) principal / effectiveTermYears);

        // 연 이자 상환액: 원래 기간 EPI 스케줄에서 현재 회차부터 12개월 이자 합산
        long annualInterestPayment = calculateScheduleBasedAnnualInterest(
                principal, interestRate, termMonths, expirationDate);

        log.info("신용대출 분리 계산 결과 - 연 원금(치환기간 기준): {}, 연 이자(원래기간 스케줄 기준): {}",
                annualPrincipalPayment, annualInterestPayment);

        return annualPrincipalPayment + annualInterestPayment;
    }

    /**
     * 주택담보대출 연 원리금상환액 계산 (상환방식별 분기)
     *
     * <p>MDT(만기일시상환) 상환기간 치환이 발생한 경우 (기존 대출에서 expirationDate 존재 시):
     * <ul>
     *   <li>연 원금 상환액: 치환된 상환기간(최대 10년) 기준</li>
     *   <li>연 이자 상환액: MDT 실제 이자 = 원금 × 연이자율 (원금 미감소로 이자 일정)</li>
     * </ul>
     *
     * @param principal         대출 원금
     * @param interestRate      연 이자율 (%)
     * @param termMonths        상환기간 (개월)
     * @param gracePeriodMonths 거치기간 (개월)
     * @param repaymentType     상환 방식
     * @param expirationDate    만기일 (기존 대출 MDT 분리 계산용, 신규 대출은 null)
     * @return 연 원리금상환액
     */
    private long calculateMortgageAnnualPayment(Long principal, Double interestRate, int termMonths,
                                                  int gracePeriodMonths, RepaymentType repaymentType,
                                                  LocalDate expirationDate) {
        switch (repaymentType) {
            case EP:
                // 원금균등 상환
                return calculateEqualPrincipalAnnualPayment(principal, interestRate, termMonths, gracePeriodMonths);

            case MDT:
                // 만기일시 상환: 상환기간 10년 초과 시 10년으로 치환
                int mdtTermMonths = Math.min(termMonths, 120); // 10년 = 120개월
                int mdtEffectiveRepayment = mdtTermMonths - gracePeriodMonths;
                if (mdtEffectiveRepayment <= 0) {
                    mdtEffectiveRepayment = mdtTermMonths;
                }

                // 기존 대출이고 치환이 발생한 경우: 원금/이자 분리 계산
                if (expirationDate != null && mdtTermMonths != termMonths) {
                    log.info("MDT 상환기간 치환 - 원래: {}개월 → 치환: {}개월 (원금/이자 분리 계산)",
                            termMonths, mdtTermMonths);

                    // 연 원금 상환액: 치환된 기간 기준 (원금 / 치환 기간(년))
                    double mdtEffectiveYears = mdtEffectiveRepayment / 12.0;
                    long annualPrincipalPayment = mdtEffectiveYears > 0
                            ? Math.round((double) principal / mdtEffectiveYears) : 0L;

                    // 연 이자 상환액: MDT 실제 이자 (원금이 만기까지 유지되므로 이자 일정)
                    // = 원금 × 연이자율 (원래 상환기간과 무관하게 동일)
                    long annualInterestPayment = Math.round(principal * interestRate / 100.0);

                    log.info("MDT 분리 계산 결과 - 연 원금(치환기간 기준): {}, 연 이자(원금×금리): {}",
                            annualPrincipalPayment, annualInterestPayment);

                    return annualPrincipalPayment + annualInterestPayment;
                }

                // 치환 없는 경우 또는 신규 대출: 기존 로직 유지
                return calculateEqualPrincipalInterestAnnualPayment(principal, interestRate, mdtEffectiveRepayment);

            case GG:
                // 체증식 상환: 원리금균등과 동일하게 계산
            case EPI:
            default:
                // 원리금균등 상환
                int effectiveRepayment = termMonths - gracePeriodMonths;
                if (effectiveRepayment <= 0) {
                    effectiveRepayment = termMonths;
                }
                return calculateEqualPrincipalInterestAnnualPayment(principal, interestRate, effectiveRepayment);
        }
    }

    /**
     * EPI 상환 스케줄 기반 연 이자 계산 (신용대출 상환기간 치환 케이스 전용)
     *
     * <p>대출 실행일을 (만기일 - 원래 상환기간)으로 역산한 후,
     * 현재 날짜에 해당하는 회차부터 12개월간 이자를 합산한다.
     *
     * <p>이자는 매월 잔액 기준으로 계산되므로, 상환이 진행될수록 이자가 감소한다.
     *
     * @param principal      대출 원금 (실행액 우선)
     * @param interestRate   연 이자율 (%)
     * @param termMonths     원래 상환기간 (개월)
     * @param expirationDate 만기일 (대출 실행일 역산용)
     * @return 현재 회차부터 12개월간 이자 합산액
     */
    private long calculateScheduleBasedAnnualInterest(Long principal, Double interestRate,
                                                       int termMonths, LocalDate expirationDate) {
        if (principal == 0 || interestRate == null || interestRate == 0
                || termMonths == 0 || expirationDate == null) {
            return 0L;
        }

        // 대출 실행일 역산: 만기일 - 원래 상환기간
        LocalDate loanStartDate = expirationDate.minusMonths(termMonths);

        // 경과 개월 수 (음수이면 아직 대출 실행 전 → 1회차부터 계산)
        long elapsedMonths = ChronoUnit.MONTHS.between(loanStartDate, LocalDate.now());
        if (elapsedMonths >= termMonths) {
            log.info("EPI 스케줄 이자 계산 - 이미 상환 완료 (경과: {}개월, 전체: {}개월)", elapsedMonths, termMonths);
            return 0L;
        }
        int currentInstallment = (int) Math.max(elapsedMonths, 0) + 1; // 1-indexed

        double monthlyRate = interestRate / 100.0 / 12.0;
        Long monthlyPayment = calculateMonthlyPayment(principal, interestRate, termMonths);

        // 현재 회차 직전까지 잔액 계산
        double balance = principal;
        for (int i = 1; i < currentInstallment && balance > 0; i++) {
            double interest = balance * monthlyRate;
            balance -= (monthlyPayment - interest);
            if (balance < 0) balance = 0;
        }

        // 현재 회차부터 12개월간 이자 합산 (상환 완료 회차 초과 시 중단)
        long totalInterest = 0L;
        int endInstallment = Math.min(currentInstallment + 11, termMonths); // 12개월
        for (int i = currentInstallment; i <= endInstallment && balance > 0; i++) {
            double interest = balance * monthlyRate;
            totalInterest += Math.round(interest);
            balance -= (monthlyPayment - interest);
            if (balance < 0) balance = 0;
        }

        log.info("EPI 스케줄 이자 계산 - 원래기간: {}개월, 대출실행일: {}, 현재회차: {}, 12개월 이자합산: {}",
                termMonths, loanStartDate, currentInstallment, totalInterest);

        return totalInterest;
    }

    /**
     * 원리금균등 상환 방식의 연 원리금상환액 계산
     * 총 원리금상환액 = 월상환액 × 실상환개월수
     * 연 평균 원리금 = 총 원리금상환액 / 실상환년수
     *
     * @param principal              대출 원금
     * @param interestRate           연 이자율 (%)
     * @param effectiveRepaymentMonths 실상환기간 (개월, 거치기간 제외)
     * @return 연 원리금상환액
     */
    private long calculateEqualPrincipalInterestAnnualPayment(Long principal, Double interestRate,
                                                                int effectiveRepaymentMonths) {
        if (effectiveRepaymentMonths <= 0) {
            return 0L;
        }
        Long monthlyPayment = calculateMonthlyPayment(principal, interestRate, effectiveRepaymentMonths);
        long totalPayment = monthlyPayment * effectiveRepaymentMonths;
        double repaymentYears = effectiveRepaymentMonths / 12.0;
        if (repaymentYears <= 0) {
            return 0L;
        }
        return Math.round(totalPayment / repaymentYears);
    }

    /**
     * 원금균등 상환 방식의 연 원리금상환액 계산
     * 상환개시 연도 기준으로 원금상환액 계산 (거치기간 제외)
     * 연도별 원리금 합산 후 총 원리금상환액 산출
     *
     * @param principal          대출 원금
     * @param interestRate       연 이자율 (%)
     * @param termMonths         총 상환기간 (개월)
     * @param gracePeriodMonths  거치기간 (개월)
     * @return 연 원리금상환액 (연평균)
     */
    private long calculateEqualPrincipalAnnualPayment(Long principal, Double interestRate,
                                                        int termMonths, int gracePeriodMonths) {
        int effectiveRepaymentMonths = termMonths - gracePeriodMonths;
        if (effectiveRepaymentMonths <= 0) {
            effectiveRepaymentMonths = termMonths;
        }

        double annualRate = interestRate / 100.0;
        int repaymentYears = (int) Math.ceil(effectiveRepaymentMonths / 12.0);
        if (repaymentYears <= 0) {
            return 0L;
        }

        // 연 원금상환액 = 대출원금 / 실상환년수
        double annualPrincipalPayment = (double) principal / repaymentYears;

        // 연도별 원리금 합산
        double totalPayment = 0.0;
        double remainingBalance = principal;

        for (int year = 1; year <= repaymentYears; year++) {
            // 해당 연도 연이자 = 연초 잔액 × 연이자율
            double annualInterest = remainingBalance * annualRate;
            // 해당 연도 원리금 = 연 원금상환액 + 연이자
            double yearlyPayment = annualPrincipalPayment + annualInterest;
            totalPayment += yearlyPayment;
            // 연초 잔액 갱신
            remainingBalance -= annualPrincipalPayment;
            if (remainingBalance < 0) {
                remainingBalance = 0;
            }
        }

        // 연 평균 원리금 = 총 원리금상환액 / 실상환년수
        return Math.round(totalPayment / repaymentYears);
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
