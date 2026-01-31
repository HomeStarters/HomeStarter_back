package com.dwj.homestarter.roadmap.dto.response;

import lombok.*;

/**
 * 최종목표 주택 정보 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalHousingDto {

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
}
