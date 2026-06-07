package com.musicPlayer.app.artist.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class ArtistDtos {

    @Data
    public static class ArtistResponse {
        private Long id;
        private String name;
        private String bio;
        private String imageUrl;
        private String genre;
        private String country;
        private Long monthlyListeners;
        private Long followerCount;
        private boolean verified;
        private boolean following;
        private String createdAt;
    }

    @Data
    public static class CreateArtistRequest {
        @NotBlank(message = "Artist name is required")
        private String name;
        private String bio;
        private String genre;
        private String country;
    }

    @Data
    public static class UpdateArtistRequest {
        private String name;
        private String bio;
        private String genre;
        private String country;
    }
}