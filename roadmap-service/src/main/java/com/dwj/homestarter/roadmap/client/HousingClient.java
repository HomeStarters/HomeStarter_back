package com.dwj.homestarter.roadmap.client;

import com.dwj.homestarter.roadmap.client.dto.HousingInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Housing Service Feign Client
 * 최종목표 주택 정보 조회
 */
@FeignClient(
    name = "housing-service",
    url = "${feign.client.url.housing-service:http://localhost:8083}"
)
public interface HousingClient {

    /**
     * 최종목표 주택 정보 조회
     *
     * @param authorization JWT 토큰
     * @return 최종목표 주택 정보
     */
    @GetMapping("/housings/final-goal")
    HousingInfoDto getFinalGoalHousing(@RequestHeader("Authorization") String authorization);
}
