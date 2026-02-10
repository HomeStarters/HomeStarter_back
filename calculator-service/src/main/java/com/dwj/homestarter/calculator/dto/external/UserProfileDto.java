package com.dwj.homestarter.calculator.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 사용자 프로필 외부 DTO
 * User Service로부터 받아오는 데이터
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {

    /**
     * 사용자 ID
     */
    private String userId;

    /**
     * 생년월일
     */
    private LocalDate birthDate;

    /**
     * 성별
     */
    private String gender;

    /**
     * 거주지
     */
    private String residence;

    /**
     * 근무지
     */
    private String workLocation;

    /**
     * 원천징수 소득
     */
    private Long withholdingTaxSalary;
}
