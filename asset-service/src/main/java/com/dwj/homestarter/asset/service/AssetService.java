package com.dwj.homestarter.asset.service;

import com.dwj.homestarter.asset.domain.AssetSummary;
import com.dwj.homestarter.asset.domain.OwnerType;
import com.dwj.homestarter.asset.dto.request.CreateAssetRequest;
import com.dwj.homestarter.asset.dto.request.UpdateAssetRequest;
import com.dwj.homestarter.asset.dto.response.AssetListResponse;
import com.dwj.homestarter.asset.dto.response.AssetResponse;

/**
 * 자산정보 관리 서비스 인터페이스
 * 자산정보의 생성, 조회, 수정, 삭제 비즈니스 로직 정의
 */
public interface AssetService {

    /**
     * 본인 자산정보 생성
     *
     * @param userId  사용자 ID
     * @param request 자산정보 생성 요청
     * @return 생성된 자산정보
     */
    AssetResponse createSelfAssets(String userId, CreateAssetRequest request);

    /**
     * 배우자 자산정보 생성
     *
     * @param userId  사용자 ID
     * @param request 자산정보 생성 요청
     * @return 생성된 자산정보
     */
    AssetResponse createSpouseAssets(String userId, CreateAssetRequest request);

    /**
     * 자산정보 조회 (본인/배우자 필터링 가능)
     *
     * @param userId    사용자 ID
     * @param ownerType 소유자 유형 (null이면 전체 조회)
     * @return 자산정보 목록 및 합산 정보
     */
    AssetListResponse getAssets(String userId, OwnerType ownerType);

    /**
     * 자산정보 수정
     *
     * @param id      자산정보 ID
     * @param userId  사용자 ID
     * @param request 자산정보 수정 요청
     * @return 수정된 자산정보
     */
    AssetResponse updateAsset(String id, String userId, UpdateAssetRequest request);

    /**
     * 자산정보 삭제
     *
     * @param id     자산정보 ID
     * @param userId 사용자 ID
     */
    void deleteAsset(String id, String userId);

    /**
     * 자산 총액 계산
     *
     * @param userId    사용자 ID
     * @param ownerType 소유자 유형
     * @return 자산 요약 정보
     */
    AssetSummary calculateTotals(String userId, OwnerType ownerType);

    /**
     * 사용자 ID로 자산정보 직접 생성 (중복 체크 없음)
     * 수정할 자산정보가 없을 때 Insert부터 하기 위한 용도
     *
     * @param userId    사용자 ID
     * @param ownerType 소유자 유형 (기본값: SELF)
     * @param request   자산정보 생성 요청
     * @return 생성된 자산정보
     */
    AssetResponse createAssetByUserId(String userId, OwnerType ownerType, CreateAssetRequest request);

    /**
     * 자산상세정보 삭제
     * 자산유형에 따라 해당 테이블에서 항목을 삭제하고 총액을 차감
     *
     * @param assetType 자산유형 (assets, loans, monthlyIncome, monthlyExpense)
     * @param itemId    삭제할 항목 ID
     * @param userId    사용자 ID
     */
    void deleteAssetItem(String assetType, String itemId, String userId);
}
