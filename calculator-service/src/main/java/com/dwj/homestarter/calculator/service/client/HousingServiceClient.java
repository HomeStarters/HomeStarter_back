package com.dwj.homestarter.calculator.service.client;

import com.dwj.homestarter.calculator.dto.external.wrapper.ApiResponse;
import com.dwj.homestarter.calculator.dto.external.wrapper.HousingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Housing Service Feign 클라이언트
 */
@FeignClient(name = "housing-service", url = "${service.url.housing}")
public interface HousingServiceClient {

    /**
     * 주택 정보 조회
     *
     * @param housingId 주택 ID
     * @return ApiResponse 래퍼로 감싼 주택 정보
     */
    @GetMapping("/housings/{housingId}")
    ApiResponse<HousingResponse> getHousingInfo(@PathVariable("housingId") String housingId);
}
