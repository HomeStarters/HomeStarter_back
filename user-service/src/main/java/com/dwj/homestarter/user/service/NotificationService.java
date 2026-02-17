package com.dwj.homestarter.user.service;

import com.dwj.homestarter.user.dto.response.NotificationResponse;
import com.dwj.homestarter.user.dto.response.UnreadCountResponse;
import com.dwj.homestarter.user.repository.entity.NotificationType;

import java.util.List;

/**
 * 알림 서비스 인터페이스
 *
 * @author homestarter
 * @since 1.0.0
 */
public interface NotificationService {

    List<NotificationResponse> getNotifications(String userId);

    void markAsRead(String userId, Long notificationId);

    UnreadCountResponse getUnreadCount(String userId);

    void createNotification(String userId, NotificationType type, String title, String message, String referenceId);
}
