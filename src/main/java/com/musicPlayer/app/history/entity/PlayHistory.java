package com.musicPlayer.app.history.entity;



import com.musicPlayer.app.common.util.BaseEntity;
import com.musicPlayer.app.song.entity.Song;
import com.musicPlayer.app.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "play_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayHistory extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Column(name = "played_at")
    private java.time.LocalDateTime playedAt;

    @Column(name = "completed")
    @Builder.Default
    private boolean completed = false;

    @Column(name = "play_duration_seconds")
    private Integer playDurationSeconds;
}