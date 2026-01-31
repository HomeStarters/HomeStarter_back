package com.dwj.homestarter.user.dto.request;

import com.dwj.homestarter.user.repository.entity.Gender;
import com.dwj.homestarter.user.repository.entity.InvestmentPropensity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 프로필 수정 요청 DTO
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {

    /**
     * 생년월일
     */
    private LocalDate birthDate;

    /**
     * 성별 (MALE, FEMALE)
     */
    private Gender gender;

    /**
     * 현재 거주지 주소
     */
    @Size(min = 5, max = 50, message = "주소는 5자 이상 50자 이하여야 합니다")
    private String currentAddress;

    /**
     * 본인 직장 주소
     */
    @Size(min = 5, max = 50, message = "주소는 5자 이상 50자 이하여야 합니다")
    private String userWorkplaceAddress;

    /**
     * 배우자 직장 주소 (선택)
     */
    @Size(min = 5, max = 50, message = "주소는 5자 이상 50자 이하여야 합니다")
    private String spouseWorkplaceAddress;

//    /**
//     * 현재 거주지 주소
//     */
//    @Valid
//    private AddressRequest currentAddress;
//
//    /**
//     * 본인 직장 주소
//     */
//    @Valid
//    private AddressRequest userWorkplaceAddress;
//
//    /**
//     * 배우자 직장 주소 (선택)
//     */
//    @Valid
//    private AddressRequest spouseWorkplaceAddress;

    /**
     * 투자 성향
     */
    private InvestmentPropensity investmentPropensity;
}
