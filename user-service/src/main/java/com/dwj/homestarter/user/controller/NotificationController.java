package com.dwj.homestarter.user.controller;

import com.dwj.homestarter.common.dto.ApiResponse;
import com.dwj.homestarter.user.dto.response.NotificationResponse;
import com.dwj.homestarter.user.dto.response.UnreadCountResponse;
import com.dwj.homestarter.user.service.NotificationService;
import com.dwj.homestarter.user.service.jwt.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 알림 컨트롤러
 *
 * 알림 조회, 읽음 처리, 읽지 않은 알림 수 조회 API 제공
 *
 * @author homestarter
 * @since 1.0.0
 */
@Tag(name = "Notification Service", description = "알림 관리 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림 목록 조회
     *
     * @param principal 인증된 사용자 정보
     * @return 알림 목록
     */
    @Operation(summary = "알림 목록 조회", description = "내 알림 목록을 최신순으로 조회합니다")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<NotificationResponse> response = notificationService.getNotifications(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("알림 목록 조회에 성공했습니다", response));
    }

    /**
     * 알림 읽음 처리
     *
     * @param notificationId 알림 ID
     * @param principal 인증된 사용자 정보
     * @return 응답
     */
    @Operation(summary = "알림 읽음 처리", description = "알림을 읽음 상태로 변경합니다")
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAsRead(principal.getUserId(), notificationId);
        return ResponseEntity.ok(ApiResponse.success("알림이 읽음 처리되었습니다"));
    }

    /**
     * 읽지 않은 알림 수 조회
     *
     * @param principal 인증된 사용자 정보
     * @return 읽지 않은 알림 수
     */
    @Operation(summary = "읽지 않은 알림 수", description = "읽지 않은 알림의 수를 조회합니다")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        UnreadCountResponse response = notificationService.getUnreadCount(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("읽지 않은 알림 수 조회에 성공했습니다", response));
    }
}
