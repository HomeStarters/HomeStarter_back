package com.dwj.homestarter.calculator.dto.external.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User Service 실제 응답 구조
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private LocalDate birthDate;
    private String gender;
    private String currentAddress;
    private String userWorkplaceAddress;
    private String spouseWorkplaceAddress;
    private Long withholdingTaxSalary;
    private String investmentPropensity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
