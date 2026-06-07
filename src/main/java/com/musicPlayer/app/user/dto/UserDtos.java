package com.musicPlayer.app.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class UserDtos {

    @Data
    public static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private String profileImage;
        private String role;
        private String status;
        private boolean emailVerified;
        private String country;
        private String dateOfBirth;
        private boolean premium;
        private String premiumExpiresAt;
        private Long monthlyPlayCount;
        private String createdAt;
    }

    @Data
    public static class UpdateProfileRequest {
        @NotBlank(message = "Name is required")
        private String name;
        private String country;
        private String dateOfBirth;
    }

    @Data
    public static class AdminUpdateUserRequest {
        private String name;
        private String role;
        private String status;
        private String email;
    }
}