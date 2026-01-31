package com.dwj.homestarter.calculator.service.client;

import com.dwj.homestarter.calculator.dto.external.wrapper.AssetListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Asset Service Feign 클라이언트
 */
@FeignClient(name = "asset-service", url = "${service.url.asset}")
public interface AssetServiceClient {

    /**
     * 자산 정보 조회 (JWT 토큰 기반 인증)
     *
     * @return 자산 목록 응답 (assets + combinedSummary)
     */
    @GetMapping("/api/v1/assets")
    AssetListResponse getAssetInfo();
}
