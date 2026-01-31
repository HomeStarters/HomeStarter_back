package com.dwj.homestarter.asset.repository.jpa;

import com.dwj.homestarter.asset.repository.entity.AssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 자산정보 리포지토리
 * 자산정보 데이터 액세스 인터페이스
 */
@Repository
public interface AssetRepository extends JpaRepository<AssetEntity, String> {

    /**
     * 사용자 ID와 소유자 유형으로 자산정보 조회
     *
     * @param userId    사용자 ID
     * @param ownerType 소유자 유형 (SELF/SPOUSE)
     * @return 자산정보 Optional
     */
    Optional<AssetEntity> findByUserIdAndOwnerType(String userId, String ownerType);

    /**
     * 사용자 ID로 모든 자산정보 조회 (본인 + 배우자)
     *
     * @param userId 사용자 ID
     * @return 자산정보 목록
     */
    List<AssetEntity> findByUserId(String userId);

    /**
     * 사용자 ID와 소유자 유형으로 자산정보 존재 여부 확인
     *
     * @param userId    사용자 ID
     * @param ownerType 소유자 유형 (SELF/SPOUSE)
     * @return 존재 여부
     */
    boolean existsByUserIdAndOwnerType(String userId, String ownerType);

    /**
     * ID로 자산정보 삭제
     *
     * @param id 자산정보 ID
     */
    void deleteById(String id);
}
