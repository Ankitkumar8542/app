package com.musicPlayer.app.album.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

public class AlbumDtos {

    @Data
    public static class AlbumResponse {
        private Long id;
        private String title;
        private String coverImage;
        private String releaseDate;
        private String type;
        private String description;
        private Integer totalTracks;
        private String totalDuration;
        private ArtistInfo artist;
        private List<SongInfo> songs;
        private String createdAt;

        @Data
        public static class ArtistInfo {
            private Long id;
            private String name;
            private String imageUrl;
            private boolean verified;
        }

        @Data
        public static class SongInfo {
            private Long id;
            private String title;
            private Integer durationSeconds;
            private String formattedDuration;
            private Integer trackNumber;
            private Long playCount;
            private boolean premium;
        }
    }

    @Data
    public static class CreateAlbumRequest {
        @NotBlank(message = "Album title is required")
        private String title;

        @NotNull(message = "Artist ID is required")
        private Long artistId;

        private String releaseDate;
        private String type;
        private String description;
    }

    @Data
    public static class UpdateAlbumRequest {
        private String title;
        private String releaseDate;
        private String type;
        private String description;
    }
}