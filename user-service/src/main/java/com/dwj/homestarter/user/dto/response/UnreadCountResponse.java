package com.dwj.homestarter.user.dto.response;

import lombok.*;

/**
 * 읽지 않은 알림 수 응답 DTO
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnreadCountResponse {

    private long unreadCount;
}
