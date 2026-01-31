package com.dwj.homestarter.roadmap.dto.response;

import lombok.*;

import java.util.List;

/**
 * 주택 특징 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HousingCharacteristicsDto {

    /**
     * 예상 가격
     */
    private Long estimatedPrice;

    /**
     * 위치 설명
     */
    private String location;

    /**
     * 주택 타입
     */
    private String type;

    /**
     * 주요 특징 목록
     */
    private List<String> features;
}
