package com.musicPlayer.app.notification.dto;


import lombok.Data;

public class NotificationDtos {

    @Data
    public static class NotificationResponse {
        private Long id;
        private String title;
        private String message;
        private String type;
        private boolean read;
        private Long referenceId;
        private String referenceType;
        private String createdAt;
    }
}