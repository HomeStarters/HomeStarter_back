package com.dwj.homestarter.roadmap.client;

import com.dwj.homestarter.roadmap.client.dto.AssetSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Asset Service Feign Client
 * 자산 요약 정보 조회
 */
@FeignClient(
    name = "asset-service",
    url = "${feign.client.url.asset-service:http://localhost:8082}"
)
public interface AssetClient {

    /**
     * 자산 요약 정보 조회
     *
     * @param authorization JWT 토큰
     * @return 자산 요약 정보
     */
    @GetMapping("/assets/summary")
    AssetSummaryDto getAssetSummary(@RequestHeader("Authorization") String authorization);
}
