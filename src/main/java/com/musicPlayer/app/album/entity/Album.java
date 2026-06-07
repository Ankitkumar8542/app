package com.musicPlayer.app.album.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

import com.musicPlayer.app.artist.entity.Artist;
import com.musicPlayer.app.common.util.BaseEntity;

@Entity
@Table(name = "albums")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Album extends BaseEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;

    @Column(name = "cover_image")
    private String coverImage;

    @Column(name = "cover_image_public_id")
    private String coverImagePublicId;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AlbumType type = AlbumType.ALBUM;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @Column(name = "total_tracks")
    @Builder.Default
    private Integer totalTracks = 0;

    public enum AlbumType {
        ALBUM, SINGLE, EP, COMPILATION
    }
}