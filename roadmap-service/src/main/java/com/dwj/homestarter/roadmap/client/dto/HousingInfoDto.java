package com.dwj.homestarter.roadmap.client.dto;

import lombok.*;

/**
 * Housing Service로부터 받는 주택 정보 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HousingInfoDto {

    /**
     * 주택 ID
     */
    private String id;

    /**
     * 주택 이름
     */
    private String name;

    /**
     * 입주 예정일 (yyyy-MM)
     */
    private String moveInDate;

    /**
     * 주택 가격
     */
    private Long price;

    /**
     * 위치
     */
    private String location;

    /**
     * 타입 (예: 전용 84㎡)
     */
    private String type;
}
