package com.dwj.homestarter.user.dto.response;

import com.dwj.homestarter.user.repository.entity.Gender;
import com.dwj.homestarter.user.repository.entity.InvestmentPropensity;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 프로필 응답 DTO
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    /**
     * 사용자 아이디
     */
    private String userId;

    /**
     * 이름
     */
    private String name;

    /**
     * 이메일
     */
    private String email;

    /**
     * 휴대폰 번호
     */
    private String phoneNumber;

    /**
     * 생년월일
     */
    private LocalDate birthDate;

    /**
     * 성별
     */
    private Gender gender;

    /**
     * 현재 거주지 주소
     */
    private String currentAddress;

    /**
     * 본인 직장 주소
     */
    private String userWorkplaceAddress;

    /**
     * 배우자 직장 주소
     */
    private String spouseWorkplaceAddress;

    /**
     * 원천징수연봉
     */
    private Long withholdingTaxSalary;

//    /**
//     * 현재 거주지 주소
//     */
//    private AddressResponse currentAddress;
//
//    /**
//     * 본인 직장 주소
//     */
//    private AddressResponse userWorkplaceAddress;
//
//    /**
//     * 배우자 직장 주소
//     */
//    private AddressResponse spouseWorkplaceAddress;

    /**
     * 투자 성향
     */
    private InvestmentPropensity investmentPropensity;

    /**
     * 생성 일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    private LocalDateTime updatedAt;
}
