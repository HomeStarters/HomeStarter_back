package com.dwj.homestarter.roadmap.client.dto;

import lombok.*;

/**
 * LLM API 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LlmRequest {

    /**
     * 모델명 (예: gpt-4)
     */
    private String model;

    /**
     * 프롬프트 내용
     */
    private String prompt;

    /**
     * 응답 최대 토큰 수
     */
    private Integer maxTokens;

    /**
     * Temperature (창의성 조절, 0.0 ~ 1.0)
     */
    private Double temperature;
}
