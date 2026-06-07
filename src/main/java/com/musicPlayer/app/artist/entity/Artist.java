package com.musicPlayer.app.artist.entity;


import com.musicPlayer.app.common.util.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "artists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Artist extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_public_id")
    private String imagePublicId;

    private String genre;

    private String country;

    @Column(name = "monthly_listeners")
    @Builder.Default
    private Long monthlyListeners = 0L;

    @Column(name = "follower_count")
    @Builder.Default
    private Long followerCount = 0L;

    @Column(name = "is_verified")
    @Builder.Default
    private boolean verified = false;
}