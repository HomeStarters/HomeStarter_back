package com.dwj.homestarter.user.dto.response;

import com.dwj.homestarter.user.repository.entity.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private String referenceId;
    private boolean isRead;
    private LocalDateTime createdAt;
}
