package com.musicPlayer.app.song.dto;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Set;

public class SongDtos {

    @Data
    public static class SongResponse {
        private Long id;
        private String title;
        private String audioUrl;
        private String coverImage;
        private Integer durationSeconds;
        private String formattedDuration;
        private Long playCount;
        private Long likeCount;
        private Integer trackNumber;
        private String lyrics;
        private boolean premium;
        private Integer releaseYear;
        private String language;
        private String status;
        private ArtistInfo artist;
        private AlbumInfo album;
        private List<CategoryInfo> categories;
        private List<ArtistInfo> featuredArtists;
        private boolean liked;
        private String createdAt;

        @Data
        public static class ArtistInfo {
            private Long id;
            private String name;
            private String imageUrl;
            private boolean verified;
        }

        @Data
        public static class AlbumInfo {
            private Long id;
            private String title;
            private String coverImage;
            private String releaseDate;
        }

        @Data
        public static class CategoryInfo {
            private Long id;
            private String name;
            private String color;
        }
    }

    @Data
    public static class CreateSongRequest {
        @NotBlank(message = "Title is required")
        private String title;

        @NotNull(message = "Artist ID is required")
        private Long artistId;

        private Long albumId;
        private Integer durationSeconds;
        private Integer trackNumber;
        private String lyrics;
        private boolean premium;
        private Integer releaseYear;
        private String language;
        private Set<Long> categoryIds;
        private Set<Long> featuredArtistIds;
    }

    @Data
    public static class UpdateSongRequest {
        private String title;
        private Long artistId;
        private Long albumId;
        private Integer durationSeconds;
        private Integer trackNumber;
        private String lyrics;
        private Boolean premium;
        private Integer releaseYear;
        private String language;
        private Set<Long> categoryIds;
        private Set<Long> featuredArtistIds;
    }

    @Data
    public static class SongSearchRequest {
        private String query;
        private Long artistId;
        private Long albumId;
        private Long categoryId;
        private String language;
        private Boolean premium;
        private Integer minDuration;
        private Integer maxDuration;
        private String sortBy;
        private String sortDir;
    }
}