package com.dwj.homestarter.user.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자 프로필 정보 엔티티
 *
 * 사용자 상세 프로필 정보를 저장하는 엔티티
 *
 * @author homestarter
 * @since 1.0.0
 */
@Entity
@Table(name = "user_profiles", schema = "user_service")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileEntity {

    /**
     * 기본키 (자동 증가)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 아이디 (FK)
     */
    @Column(name = "user_id", nullable = false, unique = true, length = 50)
    private String userId;

    /**
     * 생년월일
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * 성별 (MALE, FEMALE)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    /**
     * 현재 거주지 주소
     */
    @Column(name = "current_address", length = 50)
    private String currentAddress;

    /**
     * 본인 직장 주소
     */
    @Column(name = "user_workplace_address", length = 50)
    private String userWorkplaceAddress;

    /**
     * 배우자 직장 주소
     */
    @Column(name = "spouse_workplace_address", length = 50)
    private String spouseWorkplaceAddress;

    /**
     * 원천징수연봉
     */
    @Column(name = "withholding_tax_salary", nullable = false)
    private Long withholdingTaxSalary;

//    /**
//     * 현재 거주지 주소
//     */
//    @Embedded
//    @AttributeOverrides({
//            @AttributeOverride(name = "roadAddress", column = @Column(name = "current_road_address")),
//            @AttributeOverride(name = "jibunAddress", column = @Column(name = "current_jibun_address")),
//            @AttributeOverride(name = "postalCode", column = @Column(name = "current_postal_code")),
//            @AttributeOverride(name = "latitude", column = @Column(name = "current_latitude")),
//            @AttributeOverride(name = "longitude", column = @Column(name = "current_longitude"))
//    })
//    private AddressEmbeddable currentAddress;

//    /**
//     * 본인 직장 주소
//     */
//    @Embedded
//    @AttributeOverrides({
//            @AttributeOverride(name = "roadAddress", column = @Column(name = "user_workplace_road_address")),
//            @AttributeOverride(name = "jibunAddress", column = @Column(name = "user_workplace_jibun_address")),
//            @AttributeOverride(name = "postalCode", column = @Column(name = "user_workplace_postal_code")),
//            @AttributeOverride(name = "latitude", column = @Column(name = "user_workplace_latitude")),
//            @AttributeOverride(name = "longitude", column = @Column(name = "user_workplace_longitude"))
//    })
//    private AddressEmbeddable userWorkplaceAddress;

//    /**
//     * 배우자 직장 주소
//     */
//    @Embedded
//    @AttributeOverrides({
//            @AttributeOverride(name = "roadAddress", column = @Column(name = "spouse_workplace_road_address")),
//            @AttributeOverride(name = "jibunAddress", column = @Column(name = "spouse_workplace_jibun_address")),
//            @AttributeOverride(name = "postalCode", column = @Column(name = "spouse_workplace_postal_code")),
//            @AttributeOverride(name = "latitude", column = @Column(name = "spouse_workplace_latitude")),
//            @AttributeOverride(name = "longitude", column = @Column(name = "spouse_workplace_longitude"))
//    })
//    private AddressEmbeddable spouseWorkplaceAddress;

    /**
     * 투자 성향 (HIGH, MEDIUM, LOW)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "investment_propensity", length = 10)
    private InvestmentPropensity investmentPropensity;

    /**
     * 생성 시간
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 프로필 정보 업데이트
     *
     * @param currentAddress 현재 거주지 주소
     * @param userWorkplaceAddress 본인 직장 주소
     * @param spouseWorkplaceAddress 배우자 직장 주소
     * @param investmentPropensity 투자 성향
     * @param birthDate 생년월일
     * @param gender 성별
     * @param withholdingTaxSalary 원천징수 연봉
     */
    public void updateProfile(String currentAddress,
                              String userWorkplaceAddress,
                              String spouseWorkplaceAddress,
                              LocalDate birthDate,
                              Gender gender,
                             InvestmentPropensity investmentPropensity,
                              Long withholdingTaxSalary) {
        if (currentAddress != null) {
            this.currentAddress = currentAddress;
        }
        if (userWorkplaceAddress != null) {
            this.userWorkplaceAddress = userWorkplaceAddress;
        }
        if (spouseWorkplaceAddress != null) {
            this.spouseWorkplaceAddress = spouseWorkplaceAddress;
        }
        if (birthDate != null) {
            this.birthDate = birthDate;
        }
        if (gender != null) {
            this.gender = gender;
        }
        if (investmentPropensity != null) {
            this.investmentPropensity = investmentPropensity;
        }
        if (withholdingTaxSalary != null) {
            this.withholdingTaxSalary = withholdingTaxSalary;
        }
    }
}
