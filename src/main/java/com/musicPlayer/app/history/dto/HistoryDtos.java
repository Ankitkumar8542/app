package com.musicPlayer.app.history.dto;

import lombok.Data;

public class HistoryDtos {

    @Data
    public static class HistoryResponse {
        private Long id;
        private String playedAt;
        private boolean completed;
        private Integer playDurationSeconds;
        private SongInfo song;

        @Data
        public static class SongInfo {
            private Long id;
            private String title;
            private String coverImage;
            private Integer durationSeconds;
            private String artistName;
        }
    }

    @Data
    public static class RecordPlayRequest {
        private Long songId;
        private Integer durationSeconds;
        private boolean completed;
    }
}