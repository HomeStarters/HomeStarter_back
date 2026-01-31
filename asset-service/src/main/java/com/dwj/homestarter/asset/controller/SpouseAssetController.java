package com.dwj.homestarter.asset.controller;

import com.dwj.homestarter.asset.config.jwt.UserPrincipal;
import com.dwj.homestarter.asset.dto.request.CreateAssetRequest;
import com.dwj.homestarter.asset.dto.response.AssetResponse;
import com.dwj.homestarter.asset.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 배우자 자산정보 관리 컨트롤러
 * 배우자의 자산, 대출, 월소득, 월지출 정보 입력 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/assets/spouse")
@RequiredArgsConstructor
@Tag(name = "Spouse Assets", description = "배우자 자산정보 관리")
public class SpouseAssetController {

    private final AssetService assetService;

    /**
     * 배우자 자산정보 입력
     * 배우자의 자산, 대출, 월소득, 월지출 정보를 입력합니다.
     * 각 카테고리별로 복수 항목을 등록할 수 있습니다.
     * 배우자 없음 체크 시 모든 값이 0으로 설정됩니다.
     *
     * @param userPrincipal 인증된 사용자 정보
     * @param request       자산정보 생성 요청
     * @return 생성된 자산정보
     */
    @PostMapping
    @Operation(
            summary = "배우자 자산정보 입력",
            description = "배우자의 자산, 대출, 월소득, 월지출 정보를 입력합니다. 각 카테고리별로 복수 항목을 등록할 수 있습니다. 배우자 없음 체크 시 모든 값이 0으로 설정됩니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<AssetResponse> createSpouseAssets(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateAssetRequest request) {

        log.info("배우자 자산정보 입력 요청 - userId: {}", userPrincipal.getUserId());

        AssetResponse response = assetService.createSpouseAssets(userPrincipal.getUserId(), request);

        log.info("배우자 자산정보 입력 완료 - userId: {}", userPrincipal.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
