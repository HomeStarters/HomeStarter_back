package com.dwj.homestarter.loan.dto;

import com.dwj.homestarter.loan.domain.LoanProduct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 대출상품 DTO
 *
 * 대출상품 정보 전송 객체
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProductDTO {

    /**
     * 대출상품 ID
     */
    private Long id;

    /**
     * 대출이름
     */
    private String name;

    /**
     * 대출한도 (원)
     */
    private Long loanLimit;

//    /**
//     * LTV 한도 (%)
//     */
//    private Double ltvLimit;
//
//    /**
//     * DTI 한도 (%)
//     */
//    private Double dtiLimit;

    /**
     * DSR 한도 (%)
     */
    private Double dsrLimit;

    /**
     * LTV 적용 여부
     */
    private Boolean isApplyLtv;

    /**
     * DTI 적용 여부
     */
    private Boolean isApplyDti;

    /**
     * DSR 적용 여부
     */
    private Boolean isApplyDsr;

    /**
     * 금리 (연 %)
     */
    private Double interestRate;

    /**
     * 대상주택
     */
    private String targetHousing;

    /**
     * 소득요건
     */
    private String incomeRequirement;

    /**
     * 신청자요건
     */
    private String applicantRequirement;

    /**
     * 비고
     */
    private String remarks;

    /**
     * 활성화 여부
     */
    private Boolean active;

    /**
     * 등록일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    private LocalDateTime updatedAt;

    /**
     * Entity를 DTO로 변환
     *
     * @param entity LoanProduct 엔티티
     * @return LoanProductDTO
     */
    public static LoanProductDTO from(LoanProduct entity) {
        if (entity == null) {
            return null;
        }
        return LoanProductDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .loanLimit(entity.getLoanLimit())
//                .ltvLimit(entity.getLtvLimit())
//                .dtiLimit(entity.getDtiLimit())
                .dsrLimit(entity.getDsrLimit())
                .isApplyLtv(entity.getIsApplyLtv())
                .isApplyDti(entity.getIsApplyDti())
                .isApplyDsr(entity.getIsApplyDsr())
                .interestRate(entity.getInterestRate())
                .targetHousing(entity.getTargetHousing())
                .incomeRequirement(entity.getIncomeRequirement())
                .applicantRequirement(entity.getApplicantRequirement())
                .remarks(entity.getRemarks())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
