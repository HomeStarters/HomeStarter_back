package com.dwj.homestarter.user.service.impl;

import com.dwj.homestarter.common.exception.BusinessException;
import com.dwj.homestarter.common.exception.NotFoundException;
import com.dwj.homestarter.user.dto.response.NotificationResponse;
import com.dwj.homestarter.user.dto.response.UnreadCountResponse;
import com.dwj.homestarter.user.repository.entity.NotificationEntity;
import com.dwj.homestarter.user.repository.entity.NotificationType;
import com.dwj.homestarter.user.repository.jpa.NotificationRepository;
import com.dwj.homestarter.user.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 알림 서비스 구현체
 *
 * @author homestarter
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<NotificationResponse> getNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void markAsRead(String userId, Long notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("알림을 찾을 수 없습니다"));

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("NOTIFICATION_001", "본인의 알림만 읽음 처리할 수 있습니다");
        }

        notification.markAsRead();
        log.info("알림 읽음 처리 - userId: {}, notificationId: {}", userId, notificationId);
    }

    @Override
    public UnreadCountResponse getUnreadCount(String userId) {
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return UnreadCountResponse.builder()
                .unreadCount(count)
                .build();
    }

    @Override
    @Transactional
    public void createNotification(String userId, NotificationType type, String title, String message, String referenceId) {
        NotificationEntity notification = NotificationEntity.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .build();

        notificationRepository.save(notification);
        log.info("알림 생성 - userId: {}, type: {}, referenceId: {}", userId, type, referenceId);
    }

    private NotificationResponse toResponse(NotificationEntity entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .type(entity.getType())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .referenceId(entity.getReferenceId())
                .isRead(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
