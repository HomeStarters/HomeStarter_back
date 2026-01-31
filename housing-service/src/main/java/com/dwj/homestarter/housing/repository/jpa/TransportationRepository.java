package com.dwj.homestarter.housing.repository.jpa;

import com.dwj.homestarter.housing.repository.entity.TransportationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 교통호재 Repository
 * 교통호재 데이터 영속성 관리
 */
@Repository
public interface TransportationRepository extends JpaRepository<TransportationEntity, Long> {

    /**
     * 주택별 교통호재 목록 조회
     */
    List<TransportationEntity> findByHousingId(Long housingId);

    /**
     * 주택별 교통호재 삭제
     */
    void deleteByHousingId(Long housingId);
}
