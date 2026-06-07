package com.musicPlayer.app.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.musicPlayer.app.common.exception.BadRequestException;
import com.musicPlayer.app.common.response.PageResponse;
import com.musicPlayer.app.notification.dto.NotificationDtos;
import com.musicPlayer.app.notification.entity.Notification;
import com.musicPlayer.app.notification.repository.NotificationRepository;
import com.musicPlayer.app.user.entity.User;
import com.musicPlayer.app.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<NotificationDtos.NotificationResponse> getMyNotifications(int page, int size) {
        User user = getCurrentUser();
        return PageResponse.of(notificationRepository.findByUserIdOrderByCreatedAtDesc(
                user.getId(), PageRequest.of(page, size)).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount() {
        User user = getCurrentUser();
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        User user = getCurrentUser();
        notificationRepository.markAsRead(notificationId, user.getId());
    }

    @Transactional
    public void markAllAsRead() {
        User user = getCurrentUser();
        notificationRepository.markAllAsRead(user.getId());
    }

    public void sendNotification(User user, String title, String message,
                                  Notification.NotificationType type, Long referenceId, String referenceType) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();
        notificationRepository.save(notification);
    }

    private NotificationDtos.NotificationResponse toResponse(Notification n) {
        NotificationDtos.NotificationResponse r = new NotificationDtos.NotificationResponse();
        r.setId(n.getId());
        r.setTitle(n.getTitle());
        r.setMessage(n.getMessage());
        r.setType(n.getType().name());
        r.setRead(n.isRead());
        r.setReferenceId(n.getReferenceId());
        r.setReferenceType(n.getReferenceType());
        r.setCreatedAt(n.getCreatedAt() != null ? n.getCreatedAt().toString() : null);
        return r;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}