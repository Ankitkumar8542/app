package com.musicPlayer.app.playlist.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

public class PlaylistDtos {

    @Data
    public static class PlaylistResponse {
        private Long id;
        private String name;
        private String description;
        private String coverImage;
        private boolean isPublic;
        private boolean collaborative;
        private Long followerCount;
        private int totalSongs;
        private String totalDuration;
        private OwnerInfo owner;
        private List<PlaylistSongResponse> songs;
        private String createdAt;

        @Data
        public static class OwnerInfo {
            private Long id;
            private String name;
            private String profileImage;
        }

        @Data
        public static class PlaylistSongResponse {
            private Long id;
            private Integer position;
            private String addedAt;
            private SongInfo song;

            @Data
            public static class SongInfo {
                private Long id;
                private String title;
                private String audioUrl;
                private String coverImage;
                private Integer durationSeconds;
                private String formattedDuration;
                private Long playCount;
                private ArtistInfo artist;

                @Data
                public static class ArtistInfo {
                    private Long id;
                    private String name;
                    private String imageUrl;
                }
            }
        }
    }

    @Data
    public static class CreatePlaylistRequest {
        @NotBlank(message = "Playlist name is required")
        private String name;
        private String description;
        private Boolean isPublic = true;
        private Boolean collaborative = false;
    }

    @Data
    public static class UpdatePlaylistRequest {
        private String name;
        private String description;
        private Boolean isPublic;
        private Boolean collaborative;
    }

    @Data
    public static class AddSongRequest {
        private Long songId;
        private Integer position;
    }

    @Data
    public static class ReorderSongsRequest {
        private List<SongPosition> songs;

        @Data
        public static class SongPosition {
            private Long playlistSongId;
            private Integer newPosition;
        }
    }
}