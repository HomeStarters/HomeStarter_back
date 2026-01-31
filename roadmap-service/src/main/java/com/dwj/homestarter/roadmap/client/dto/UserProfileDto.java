package com.dwj.homestarter.roadmap.client.dto;

import lombok.*;

/**
 * User Service로부터 받는 사용자 프로필 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {

    /**
     * 생년월일
     */
    private String birthDate;

    /**
     * 성별
     */
    private String gender;

    /**
     * 투자 성향 (HIGH, MEDIUM, LOW)
     */
    private String investmentPropensity;
}
