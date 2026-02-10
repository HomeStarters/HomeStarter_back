package com.dwj.homestarter.calculator.dto.external.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Housing Service 실제 응답 구조
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HousingResponse {
    private Long id;
    private String housingName;
    private String housingType;
    private BigDecimal price;
    private String moveInDate;
    private LocalDate completionDate;
    private String address;
    private RegionalCharacteristicResponse regionalCharacteristic;
    private Object complexInfo;
    private Object livingEnvironment;
    private Boolean isGoal;
    private Object transportations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 지역 특성 응답 구조
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegionalCharacteristicResponse {
        private String regionCode;
        private String regionDescription;
        private BigDecimal ltv;
        private BigDecimal dti;
    }
}
