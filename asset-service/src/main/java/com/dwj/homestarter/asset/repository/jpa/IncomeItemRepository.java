package com.dwj.homestarter.asset.repository.jpa;

import com.dwj.homestarter.asset.repository.entity.IncomeItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 월소득 항목 리포지토리
 * 월소득 항목 데이터 액세스 인터페이스
 */
@Repository
public interface IncomeItemRepository extends JpaRepository<IncomeItemEntity, String> {

    /**
     * 자산정보 ID로 월소득 항목 조회
     *
     * @param assetId 자산정보 ID
     * @return 월소득 항목 목록
     */
    List<IncomeItemEntity> findByAssetId(String assetId);

    /**
     * 자산정보 ID로 월소득 항목 삭제
     *
     * @param assetId 자산정보 ID
     */
    void deleteByAssetId(String assetId);
}
