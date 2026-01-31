package com.dwj.homestarter.housing.domain.model;

import com.dwj.homestarter.housing.domain.enums.NoiseLevel;
import com.dwj.homestarter.housing.domain.enums.SunlightLevel;
import lombok.*;

import java.util.List;

/**
 * 생활환경 정보 값 객체
 * JSONB 컬럼으로 저장될 복합 정보
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LivingEnvironment {

    /**
     * 일조량 수준
     */
    private SunlightLevel sunlightLevel;

    /**
     * 소음 수준
     */
    private NoiseLevel noiseLevel;

    /**
     * 인근 학교 목록
     */
    private List<String> nearbySchools;

    /**
     * 인근 마트 목록
     */
    private List<String> nearbyMarts;

    /**
     * 인근 병원 목록
     */
    private List<String> nearbyHospitals;
}
