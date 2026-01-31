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
 * 본인 자산정보 관리 컨트롤러
 * 본인의 자산, 대출, 월소득, 월지출 정보 입력 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/assets/self")
@RequiredArgsConstructor
@Tag(name = "Self Assets", description = "본인 자산정보 관리")
public class SelfAssetController {

    private final AssetService assetService;

    /**
     * 본인 자산정보 입력
     * 본인의 자산, 대출, 월소득, 월지출 정보를 입력합니다.
     * 각 카테고리별로 복수 항목을 등록할 수 있습니다.
     *
     * @param userPrincipal 인증된 사용자 정보
     * @param request       자산정보 생성 요청
     * @return 생성된 자산정보
     */
    @PostMapping
    @Operation(
            summary = "본인 자산정보 입력",
            description = "본인의 자산, 대출, 월소득, 월지출 정보를 입력합니다. 각 카테고리별로 복수 항목을 등록할 수 있습니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<AssetResponse> createSelfAssets(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateAssetRequest request) {

        log.info("본인 자산정보 입력 요청 - userId: {}", userPrincipal.getUserId());

        AssetResponse response = assetService.createSelfAssets(userPrincipal.getUserId(), request);

        log.info("본인 자산정보 입력 완료 - userId: {}", userPrincipal.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
