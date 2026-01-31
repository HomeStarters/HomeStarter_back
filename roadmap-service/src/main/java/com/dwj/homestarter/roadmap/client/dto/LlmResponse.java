package com.dwj.homestarter.roadmap.client.dto;

import lombok.*;

/**
 * LLM API 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LlmResponse {

    /**
     * 생성된 텍스트 결과
     */
    private String generatedText;

    /**
     * 사용된 토큰 수
     */
    private Integer tokensUsed;

    /**
     * 모델명
     */
    private String model;
}
