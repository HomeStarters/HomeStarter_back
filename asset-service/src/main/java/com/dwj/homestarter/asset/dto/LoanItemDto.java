package com.dwj.homestarter.asset.dto;

import com.dwj.homestarter.asset.domain.LoanItem;
import com.dwj.homestarter.asset.domain.LoanType;
import com.dwj.homestarter.asset.domain.RepaymentType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 대출 항목 DTO
 * 대출 항목 정보 전달용 데이터 객체
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanItemDto {

    /**
     * 대출 항목 ID (응답 시에만 사용)
     */
    private String id;

    /**
     * 대출명 (예: 주택담보대출)
     */
    @NotBlank(message = "대출명은 필수입니다")
    private String name;

    /**
     * 대출 잔액 (원)
     */
    @NotNull(message = "대출 잔액은 필수입니다")
    @PositiveOrZero(message = "대출 잔액은 0 이상이어야 합니다")
    private Long amount;

    /**
     * 금리 (연 %)
     */
    @NotNull(message = "금리는 필수입니다")
    private Double interestRate;

    /**
     * 대출 유형 (주택담보, 전세, 신용, 기타)
     */
    private LoanType loanType;

    /**
     * 상환 유형 (원금균등, 원리금균등, 만기일시, 체증식)
     */
    @NotNull(message = "상환 유형은 필수입니다")
    private RepaymentType repaymentType;

    /**
     * 만기일
     */
    @NotNull(message = "만기일은 필수입니다")
    private LocalDate expirationDate;

    /**
     * 계산제외 여부
     */
    @NotNull(message = "계산제외 여부는 필수입니다")
    private Boolean isExcludingCalculation;

    /**
     * 대출실행 금액 (원)
     */
    @PositiveOrZero(message = "대출실행 금액은 0 이상이어야 합니다")
    private Long executedAmount;

    /**
     * 상환기간 (개월)
     */
    @Positive(message = "상환기간은 1 이상이어야 합니다")
    private Integer repaymentPeriod;

    /**
     * 거치기간 (개월)
     */
    @PositiveOrZero(message = "거치기간은 0 이상이어야 합니다")
    private Integer gracePeriod;

    /**
     * 도메인 객체로 변환
     *
     * @return LoanItem 도메인 객체
     */
    public LoanItem toDomain() {
        return LoanItem.builder()
                .id(id != null && !id.startsWith("temp_") ? id : UUID.randomUUID().toString())
                .name(name)
                .amount(amount)
                .interestRate(interestRate)
                .loanType(loanType != null ? loanType : LoanType.OTHER)
                .repaymentType(repaymentType)
                .expirationDate(expirationDate)
                .isExcludingCalculation(isExcludingCalculation != null ? isExcludingCalculation : false)
                .executedAmount(executedAmount)
                .repaymentPeriod(repaymentPeriod)
                .gracePeriod(gracePeriod != null ? gracePeriod : 0)
                .build();
    }

    /**
     * 도메인 객체로부터 DTO 생성
     *
     * @param item LoanItem 도메인 객체
     * @return LoanItemDto
     */
    public static LoanItemDto fromDomain(LoanItem item) {
        return LoanItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .amount(item.getAmount())
                .interestRate(item.getInterestRate())
                .loanType(item.getLoanType())
                .repaymentType(item.getRepaymentType())
                .expirationDate(item.getExpirationDate())
                .isExcludingCalculation(item.getIsExcludingCalculation())
                .executedAmount(item.getExecutedAmount())
                .repaymentPeriod(item.getRepaymentPeriod())
                .gracePeriod(item.getGracePeriod())
                .build();
    }
}
