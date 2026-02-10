package com.dwj.homestarter.loan.dto;

import com.dwj.homestarter.loan.domain.LoanProduct;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대출상품 등록 요청 DTO
 *
 * 새로운 대출상품 등록 시 사용하는 요청 객체
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLoanProductRequest {

    /**
     * 대출이름
     */
    @NotBlank(message = "대출이름은 필수입니다")
    @Size(max = 100, message = "대출이름은 100자 이하여야 합니다")
    private String name;

    /**
     * 대출한도 (원)
     */
    @NotNull(message = "대출한도는 필수입니다")
    @Min(value = 0, message = "대출한도는 0 이상이어야 합니다")
    private Long loanLimit;

//    /**
//     * LTV 한도 (%)
//     */
//    @NotNull(message = "LTV 한도는 필수입니다")
//    @DecimalMin(value = "0.0", message = "LTV 한도는 0 이상이어야 합니다")
//    @DecimalMax(value = "100.0", message = "LTV 한도는 100 이하여야 합니다")
//    private Double ltvLimit;
//
//    /**
//     * DTI 한도 (%)
//     */
//    @NotNull(message = "DTI 한도는 필수입니다")
//    @DecimalMin(value = "0.0", message = "DTI 한도는 0 이상이어야 합니다")
//    @DecimalMax(value = "100.0", message = "DTI 한도는 100 이하여야 합니다")
//    private Double dtiLimit;

    /**
     * DSR 한도 (%)
     */
    @NotNull(message = "DSR 한도는 필수입니다")
    @DecimalMin(value = "0.0", message = "DSR 한도는 0 이상이어야 합니다")
    @DecimalMax(value = "100.0", message = "DSR 한도는 100 이하여야 합니다")
    private Double dsrLimit;

    /**
     * LTV 적용 여부
     */
    @NotNull(message = "LTV 적용 여부는 필수입니다")
    private Boolean isApplyLtv;

    /**
     * DTI 적용 여부
     */
    @NotNull(message = "DTI 적용 여부는 필수입니다")
    private Boolean isApplyDti;

    /**
     * DSR 적용 여부
     */
    @NotNull(message = "DSR 적용 여부는 필수입니다")
    private Boolean isApplyDsr;

    /**
     * 금리 (연 %)
     */
    @NotNull(message = "금리는 필수입니다")
    @DecimalMin(value = "0.0", message = "금리는 0 이상이어야 합니다")
    @DecimalMax(value = "100.0", message = "금리는 100 이하여야 합니다")
    private Double interestRate;

    /**
     * 대상주택
     */
    @NotBlank(message = "대상주택은 필수입니다")
    @Size(max = 200, message = "대상주택은 200자 이하여야 합니다")
    private String targetHousing;

    /**
     * 소득요건
     */
    @Size(max = 200, message = "소득요건은 200자 이하여야 합니다")
    private String incomeRequirement;

    /**
     * 신청자요건
     */
    @Size(max = 200, message = "신청자요건은 200자 이하여야 합니다")
    private String applicantRequirement;

    /**
     * 비고
     */
    private String remarks;

    /**
     * DTO를 Entity로 변환
     *
     * @return LoanProduct 엔티티
     */
    public LoanProduct toEntity() {
        return LoanProduct.builder()
                .name(this.name)
                .loanLimit(this.loanLimit)
//                .ltvLimit(this.ltvLimit)
//                .dtiLimit(this.dtiLimit)
                .dsrLimit(this.dsrLimit)
                .isApplyLtv(this.isApplyLtv)
                .isApplyDti(this.isApplyDti)
                .isApplyDsr(this.isApplyDsr)
                .interestRate(this.interestRate)
                .targetHousing(this.targetHousing)
                .incomeRequirement(this.incomeRequirement)
                .applicantRequirement(this.applicantRequirement)
                .remarks(this.remarks)
                .active(true)
                .build();
    }
}
