package com.musicPlayer.app.song.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import com.musicPlayer.app.album.entity.Album;
import com.musicPlayer.app.artist.entity.Artist;
import com.musicPlayer.app.category.entity.Category;
import com.musicPlayer.app.common.util.BaseEntity;

@Entity
@Table(name = "songs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Song extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    @Column(nullable = false)
    private String title;

    @Column(name = "audio_url", nullable = false)
    private String audioUrl;

    @Column(name = "audio_public_id", nullable = false)
    private String audioPublicId;

    @Column(name = "cover_image")
    private String coverImage;

    @Column(name = "cover_image_public_id")
    private String coverImagePublicId;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "play_count")
    @Builder.Default
    private Long playCount = 0L;

    @Column(name = "like_count")
    @Builder.Default
    private Long likeCount = 0L;

    @Column(name = "track_number")
    private Integer trackNumber;

    @Column(columnDefinition = "TEXT")
    private String lyrics;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SongStatus status = SongStatus.ACTIVE;

    @Column(name = "is_premium")
    @Builder.Default
    private boolean premium = false;

    @Column(name = "release_year")
    private Integer releaseYear;

    private String language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "song_categories",
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "song_featured_artists",
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    @Builder.Default
    private Set<Artist> featuredArtists = new HashSet<>();

    public String getFormattedDuration() {
        if (durationSeconds == null) return "0:00";
        int mins = durationSeconds / 60;
        int secs = durationSeconds % 60;
        return String.format("%d:%02d", mins, secs);
    }

    public enum SongStatus {
        ACTIVE, INACTIVE, DELETED
    }
}