package com.dwj.homestarter.asset.repository.jpa;

import com.dwj.homestarter.asset.repository.entity.AssetItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 자산 항목 리포지토리
 * 자산 항목 데이터 액세스 인터페이스
 */
@Repository
public interface AssetItemRepository extends JpaRepository<AssetItemEntity, String> {

    /**
     * 자산정보 ID로 자산 항목 조회
     *
     * @param assetId 자산정보 ID
     * @return 자산 항목 목록
     */
    List<AssetItemEntity> findByAssetId(String assetId);

    /**
     * 자산정보 ID로 자산 항목 삭제
     *
     * @param assetId 자산정보 ID
     */
    void deleteByAssetId(String assetId);
}
