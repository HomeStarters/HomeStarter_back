package com.dwj.homestarter.housing.repository.jpa;

import com.dwj.homestarter.housing.repository.entity.HousingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 주택 Repository
 * 주택 데이터 영속성 관리
 */
@Repository
public interface HousingRepository extends JpaRepository<HousingEntity, Long> {

    /**
     * 사용자별 주택 목록 조회 (페이징)
     */
    Page<HousingEntity> findByUserId(String userId, Pageable pageable);

    /**
     * 사용자별 최종목표 주택 조회
     */
    Optional<HousingEntity> findByUserIdAndIsGoalTrue(String userId);

    /**
     * 주택 ID와 사용자 ID로 조회 (권한 검증용)
     */
    Optional<HousingEntity> findByIdAndUserId(Long id, String userId);

    /**
     * 사용자의 기존 최종목표 주택 해제
     */
    @Modifying
    @Query("UPDATE HousingEntity h SET h.isGoal = false WHERE h.userId = :userId AND h.isGoal = true")
    void clearGoalHousingByUserId(@Param("userId") String userId);

    /**
     * 주택 존재 여부 확인
     */
    boolean existsByIdAndUserId(Long id, String userId);
}
