package com.dwj.homestarter.calculator.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 주택 정보 외부 DTO
 * Housing Service로부터 받아오는 데이터
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HousingDto {

    /**
     * 주택 ID
     */
    private String housingId;

    /**
     * 주택 이름
     */
    private String name;

    /**
     * 주택 유형
     */
    private String type;

    /**
     * 가격 (원)
     */
    private Long price;

    /**
     * 입주 예정일
     */
    private LocalDate moveInDate;
}
