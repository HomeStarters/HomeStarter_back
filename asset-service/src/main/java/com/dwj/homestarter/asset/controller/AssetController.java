package com.dwj.homestarter.asset.controller;

import com.dwj.homestarter.asset.config.jwt.UserPrincipal;
import com.dwj.homestarter.asset.domain.OwnerType;
import com.dwj.homestarter.asset.dto.request.CreateAssetRequest;
import com.dwj.homestarter.asset.dto.request.UpdateAssetRequest;
import com.dwj.homestarter.asset.dto.response.AssetListResponse;
import com.dwj.homestarter.asset.dto.response.AssetResponse;
import com.dwj.homestarter.asset.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 자산정보 조회/수정/삭제 컨트롤러
 * 본인 및 배우자의 자산정보 조회, 수정, 삭제 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@Tag(name = "Assets Management", description = "자산정보 조회/수정/삭제")
public class AssetController {

    private final AssetService assetService;

    /**
     * 사용자 ID로 자산정보 직접 입력
     * 수정할 자산정보가 없을 때 Insert부터 하기 위한 용도입니다.
     * 중복 체크 없이 직접 insert합니다.
     *
     * @param userPrincipal 인증된 사용자 정보
     * @param userId        사용자 ID
     * @param ownerType     소유자 유형 (SELF=본인, SPOUSE=배우자), 기본값: SELF
     * @param request       자산정보 생성 요청
     * @return 생성된 자산정보
     */
    @PostMapping("/{userId}")
    @Operation(
            summary = "사용자 ID로 자산정보 직접 입력",
            description = "수정할 자산정보가 없을 때 Insert부터 하기 위한 용도입니다. 중복 체크 없이 직접 insert합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<AssetResponse> createAssetByUserId(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "사용자 ID")
            @PathVariable String userId,
            @Parameter(description = "소유자 유형 (SELF=본인, SPOUSE=배우자), 기본값: SELF")
            @RequestParam(required = false, defaultValue = "SELF") String ownerType,
            @Valid @RequestBody CreateAssetRequest request) {

        log.info("사용자 ID로 자산정보 직접 입력 요청 - userId: {}, ownerType: {}", userId, ownerType);

        OwnerType type = OwnerType.valueOf(ownerType);
        AssetResponse response = assetService.createAssetByUserId(userId, type, request);

        log.info("사용자 ID로 자산정보 직접 입력 완료 - userId: {}, assetId: {}", userId, response.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 자산정보 조회
     * 본인 및 배우자의 모든 자산정보를 조회합니다.
     * ownerType 파라미터로 본인/배우자를 필터링할 수 있습니다.
     *
     * @param userPrincipal 인증된 사용자 정보
     * @param ownerType     소유자 유형 필터 (SELF=본인, SPOUSE=배우자)
     * @return 자산정보 목록 및 가구 전체 합산 정보
     */
    @GetMapping
    @Operation(
            summary = "자산정보 조회",
            description = "본인 및 배우자의 모든 자산정보를 조회합니다. ownerType 파라미터로 본인/배우자를 필터링할 수 있습니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<AssetListResponse> getAssets(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "소유자 유형 필터 (SELF=본인, SPOUSE=배우자)")
            @RequestParam(required = false) String ownerType) {

        log.info("자산정보 조회 요청 - userId: {}, ownerType: {}", userPrincipal.getUserId(), ownerType);

        OwnerType type = ownerType != null ? OwnerType.valueOf(ownerType) : null;
        AssetListResponse response = assetService.getAssets(userPrincipal.getUserId(), type);

        log.info("자산정보 조회 완료 - userId: {}, 조회된 자산 수: {}", userPrincipal.getUserId(), response.getAssets().size());

        return ResponseEntity.ok(response);
    }

    /**
     * 자산정보 수정
     * 특정 자산정보를 수정합니다.
     * 전체 항목을 교체하는 방식입니다.
     * 수정 시 Calculator 서비스에 변경 이벤트를 전달하여 재계산을 트리거합니다.
     *
     * @param userPrincipal 인증된 사용자 정보
     * @param id            자산정보 ID
     * @param request       자산정보 수정 요청
     * @return 수정된 자산정보
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "자산정보 수정",
            description = "특정 자산정보를 수정합니다. 전체 항목을 교체하는 방식입니다. 수정 시 Calculator 서비스에 변경 이벤트를 전달하여 재계산을 트리거합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<AssetResponse> updateAsset(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "자산정보 ID")
            @PathVariable String id,
            @Valid @RequestBody UpdateAssetRequest request) {

        log.info("자산정보 수정 요청 - userId: {}, assetId: {}", userPrincipal.getUserId(), id);

        AssetResponse response = assetService.updateAsset(id, userPrincipal.getUserId(), request);

        log.info("자산정보 수정 완료 - userId: {}, assetId: {}", userPrincipal.getUserId(), id);

        return ResponseEntity.ok(response);
    }

    /**
     * 자산정보 삭제
     * 특정 자산정보를 삭제합니다.
     * 삭제 시 Calculator 서비스에 변경 이벤트를 전달합니다.
     *
     * @param userPrincipal 인증된 사용자 정보
     * @param id            자산정보 ID
     * @return 응답 본문 없음
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "자산정보 삭제",
            description = "특정 자산정보를 삭제합니다. 삭제 시 Calculator 서비스에 변경 이벤트를 전달합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<Void> deleteAsset(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "자산정보 ID")
            @PathVariable String id) {

        log.info("자산정보 삭제 요청 - userId: {}, assetId: {}", userPrincipal.getUserId(), id);

        assetService.deleteAsset(id, userPrincipal.getUserId());

        log.info("자산정보 삭제 완료 - userId: {}, assetId: {}", userPrincipal.getUserId(), id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 자산상세정보 삭제
     * 자산유형에 따라 해당 테이블에서 항목을 삭제하고 총액을 차감합니다.
     *
     * @param userPrincipal 인증된 사용자 정보
     * @param assetType     자산유형 (assets, loans, monthlyIncome, monthlyExpense)
     * @param id            삭제할 항목 ID
     * @return 응답 본문 없음
     */
    @DeleteMapping("/{assetType}/{id}")
    @Operation(
            summary = "자산상세정보 삭제",
            description = "자산유형에 따라 해당 테이블에서 항목을 삭제하고 assets 테이블의 총액을 차감합니다. " +
                    "자산유형: assets(자산), loans(대출), monthlyIncome(월소득), monthlyExpense(월지출)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<Void> deleteAssetItem(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "자산유형 (assets, loans, monthlyIncome, monthlyExpense)")
            @PathVariable String assetType,
            @Parameter(description = "삭제할 항목 ID")
            @PathVariable String id) {

        log.info("자산상세정보 삭제 요청 - userId: {}, assetType: {}, itemId: {}",
                userPrincipal.getUserId(), assetType, id);

        assetService.deleteAssetItem(assetType, id, userPrincipal.getUserId());

        log.info("자산상세정보 삭제 완료 - userId: {}, assetType: {}, itemId: {}",
                userPrincipal.getUserId(), assetType, id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
