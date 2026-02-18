package com.dwj.homestarter.user.controller;

import com.dwj.homestarter.common.dto.ApiResponse;
import com.dwj.homestarter.user.dto.request.HouseholdInviteRequest;
import com.dwj.homestarter.user.dto.response.HouseholdInvitationResponse;
import com.dwj.homestarter.user.dto.response.HouseholdMemberResponse;
import com.dwj.homestarter.user.service.HouseholdService;
import com.dwj.homestarter.user.service.jwt.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 가구원 관리 컨트롤러
 *
 * 가구원 초대, 수락, 거절, 목록 조회, 제거 API 제공
 *
 * @author homestarter
 * @since 1.0.0
 */
@Tag(name = "Household Service", description = "가구원 관리 API")
@RestController
@RequestMapping("/api/v1/household")
@RequiredArgsConstructor
public class HouseholdController {

    private final HouseholdService householdService;

    /**
     * 가구원 등록 요청
     *
     * @param request 초대 요청 DTO
     * @param principal 인증된 사용자 정보
     * @return 초대 응답
     */
    @Operation(summary = "가구원 등록 요청", description = "대상 사용자에게 가구원 등록 요청을 발송합니다")
    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<HouseholdInvitationResponse>> invite(
            @Valid @RequestBody HouseholdInviteRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        HouseholdInvitationResponse response = householdService.invite(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("가구원 등록 요청이 발송되었습니다", response));
    }

    /**
     * 가구원 초대 수락
     *
     * @param invitationId 초대 ID
     * @param principal 인증된 사용자 정보
     * @return 초대 응답
     */
    @Operation(summary = "가구원 초대 수락", description = "받은 가구원 등록 요청을 수락합니다")
    @PostMapping("/invite/{invitationId}/accept")
    public ResponseEntity<ApiResponse<HouseholdInvitationResponse>> acceptInvitation(
            @PathVariable String invitationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        HouseholdInvitationResponse response = householdService.acceptInvitation(principal.getUserId(), invitationId);
        return ResponseEntity.ok(ApiResponse.success("가구원 등록이 완료되었습니다", response));
    }

    /**
     * 가구원 초대 거절
     *
     * @param invitationId 초대 ID
     * @param principal 인증된 사용자 정보
     * @return 초대 응답
     */
    @Operation(summary = "가구원 초대 거절", description = "받은 가구원 등록 요청을 거절합니다")
    @PostMapping("/invite/{invitationId}/reject")
    public ResponseEntity<ApiResponse<HouseholdInvitationResponse>> rejectInvitation(
            @PathVariable String invitationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        HouseholdInvitationResponse response = householdService.rejectInvitation(principal.getUserId(), invitationId);
        return ResponseEntity.ok(ApiResponse.success("가구원 등록 요청을 거절했습니다", response));
    }

    /**
     * 가구원 목록 조회
     *
     * @param principal 인증된 사용자 정보
     * @return 가구원 목록 응답
     */
    @Operation(summary = "가구원 목록 조회", description = "내 가구에 속한 가구원 목록을 조회합니다")
    @GetMapping("/members")
    public ResponseEntity<ApiResponse<HouseholdMemberResponse>> getMembers(
            @AuthenticationPrincipal UserPrincipal principal) {
        HouseholdMemberResponse response = householdService.getMembers(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("가구원 목록 조회에 성공했습니다", response));
    }

    /**
     * 가구원 제거
     *
     * @param memberId 가구원 ID (household_members.id)
     * @param principal 인증된 사용자 정보
     * @return 응답
     */
    @Operation(summary = "가구원 제거", description = "가구에서 가구원을 제거합니다")
    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable String memberId,
            @AuthenticationPrincipal UserPrincipal principal) {
        householdService.removeMember(principal.getUserId(), memberId);
        return ResponseEntity.ok(ApiResponse.success("가구원이 제거되었습니다"));
    }

    /**
     * OWNER 권한 위임
     *
     * @param newOwnerId 권한을 위임받을 사용자 ID
     * @param principal 인증된 사용자 정보 (현재 OWNER)
     * @return 응답
     */
    @Operation(summary = "OWNER 권한 위임", description = "가구의 OWNER 권한을 다른 가구원에게 위임합니다")
    @PutMapping("/members/{newOwnerId}/delegate-owner")
    public ResponseEntity<ApiResponse<Void>> delegateOwnership(
            @PathVariable String newOwnerId,
            @AuthenticationPrincipal UserPrincipal principal) {
        householdService.delegateOwnership(principal.getUserId(), newOwnerId);
        return ResponseEntity.ok(ApiResponse.success("OWNER 권한이 위임되었습니다"));
    }

    /**
     * 대기 중인 초대 목록 조회
     *
     * @param principal 인증된 사용자 정보
     * @return 대기 중인 초대 목록
     */
    @Operation(summary = "대기 중인 초대 목록", description = "나에게 온 대기 중인 가구원 초대 목록을 조회합니다")
    @GetMapping("/invitations/pending")
    public ResponseEntity<ApiResponse<List<HouseholdInvitationResponse>>> getPendingInvitations(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<HouseholdInvitationResponse> response = householdService.getPendingInvitations(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("대기 중인 초대 목록 조회에 성공했습니다", response));
    }
}
