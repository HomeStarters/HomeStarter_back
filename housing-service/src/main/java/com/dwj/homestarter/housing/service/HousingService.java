package com.dwj.homestarter.housing.service;

import com.dwj.homestarter.housing.dto.request.HousingCreateRequest;
import com.dwj.homestarter.housing.dto.request.HousingUpdateRequest;
import com.dwj.homestarter.housing.dto.response.GoalHousingResponse;
import com.dwj.homestarter.housing.dto.response.HousingDeleteResponse;
import com.dwj.homestarter.housing.dto.response.HousingListResponse;
import com.dwj.homestarter.housing.dto.response.HousingResponse;
import org.springframework.data.domain.Pageable;

/**
 * 주택 서비스 인터페이스
 * 주택 관리 비즈니스 로직 정의
 */
public interface HousingService {

    /**
     * 주택 생성
     */
    HousingResponse createHousing(String userId, HousingCreateRequest request);

    /**
     * 주택 목록 조회
     */
    HousingListResponse getHousings(String userId, Pageable pageable);

    /**
     * 주택 상세 조회
     */
    HousingResponse getHousing(Long housingId, String userId);

    /**
     * 주택 정보 수정
     */
    HousingResponse updateHousing(Long housingId, String userId, HousingUpdateRequest request);

    /**
     * 주택 삭제
     */
    HousingDeleteResponse deleteHousing(Long housingId, String userId);

    /**
     * 최종목표 주택 설정
     */
    GoalHousingResponse setGoalHousing(Long housingId, String userId);
}
