package com.dwj.homestarter.roadmap.client;

import com.dwj.homestarter.roadmap.client.dto.LlmRequest;
import com.dwj.homestarter.roadmap.client.dto.LlmResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * LLM API Feign Client
 * AI 기반 로드맵 생성
 */
@FeignClient(
    name = "llm-api",
    url = "${feign.client.url.llm-api:https://api.openai.com}"
)
public interface LlmClient {

    /**
     * AI 로드맵 생성 요청
     *
     * @param apiKey LLM API Key
     * @param request 요청 파라미터
     * @return 생성된 로드맵 정보
     */
    @PostMapping("/v1/completions")
    LlmResponse generateRoadmap(
        @RequestHeader("Authorization") String apiKey,
        @RequestBody LlmRequest request
    );
}
