package com.dwj.homestarter.loan.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 대출상품 엔티티
 *
 * loan_products 테이블과 매핑되는 JPA 엔티티
 *
 * @author homestarter
 * @since 1.0.0
 */
@Entity
@Table(name = "loan_products", schema = "loan_service")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LoanProduct {

    /**
     * 대출상품 ID (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 대출이름
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 대출한도 (원 단위)
     */
    @Column(nullable = false)
    private Long loanLimit;

//    /**
//     * LTV 한도 (%)
//     */
//    @Column(nullable = false)
//    private Double ltvLimit;
//
//    /**
//     * DTI 한도 (%)
//     */
//    @Column(nullable = false)
//    private Double dtiLimit;

    /**
     * DSR 한도 (%)
     */
    @Column(nullable = false)
    private Double dsrLimit;

    /**
     * LTV 적용 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isApplyLtv = true;

    /**
     * DTI 적용 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isApplyDti = true;

    /**
     * DSR 적용 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isApplyDsr = true;

    /**
     * 금리 (연 %)
     */
    @Column(nullable = false)
    private Double interestRate;

    /**
     * 대상주택
     */
    @Column(nullable = false, length = 200)
    private String targetHousing;

    /**
     * 소득요건
     */
    @Column(length = 200)
    private String incomeRequirement;

    /**
     * 신청자요건
     */
    @Column(length = 200)
    private String applicantRequirement;

    /**
     * 비고
     */
    @Column(columnDefinition = "TEXT")
    private String remarks;

    /**
     * 활성화 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * 등록일시
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 대출상품 활성화
     */
    public void activate() {
        this.active = true;
    }

    /**
     * 대출상품 비활성화 (소프트 삭제)
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * 대출상품 정보 업데이트
     *
     * @param name 대출이름
     * @param loanLimit 대출한도
     * @param dsrLimit DSR 한도
     * @param isApplyLtv LTV 적용 여부
     * @param isApplyDti DTI 적용 여부
     * @param isApplyDsr DSR 적용 여부
     * @param interestRate 금리
     * @param targetHousing 대상주택
     * @param incomeRequirement 소득요건
     * @param applicantRequirement 신청자요건
     * @param remarks 비고
     * @param active 활성화 여부
     */
    public void update(String name, Long loanLimit,
                      Double dsrLimit, boolean isApplyLtv, boolean isApplyDti,
                      boolean isApplyDsr, Double interestRate, String targetHousing,
                      String incomeRequirement, String applicantRequirement,
                      String remarks, Boolean active) {
        this.name = name;
        this.loanLimit = loanLimit;
//        this.ltvLimit = ltvLimit;
//        this.dtiLimit = dtiLimit;
        this.isApplyLtv = isApplyLtv;
        this.isApplyDti = isApplyDti;
        this.isApplyDsr = isApplyDsr;
        this.dsrLimit = dsrLimit;
        this.interestRate = interestRate;
        this.targetHousing = targetHousing;
        this.incomeRequirement = incomeRequirement;
        this.applicantRequirement = applicantRequirement;
        this.remarks = remarks;
        this.active = active;
    }
}
