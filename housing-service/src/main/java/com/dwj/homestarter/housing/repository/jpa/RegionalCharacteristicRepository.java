package com.dwj.homestarter.housing.repository.jpa;

import com.dwj.homestarter.housing.domain.enums.RegionCode;
import com.dwj.homestarter.housing.repository.entity.RegionalCharacteristicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 지역 특성 레포지토리
 * 지역별 LTV, DTI 정보 조회
 */
@Repository
public interface RegionalCharacteristicRepository extends JpaRepository<RegionalCharacteristicEntity, RegionCode> {

    /**
     * 지역 코드로 지역 특성 조회
     */
    Optional<RegionalCharacteristicEntity> findByRegionCode(RegionCode regionCode);

    /**
     * 지역 코드 존재 여부 확인
     */
    boolean existsByRegionCode(RegionCode regionCode);
}
