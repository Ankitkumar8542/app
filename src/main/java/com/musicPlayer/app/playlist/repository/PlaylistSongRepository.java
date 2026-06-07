package com.musicPlayer.app.playlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicPlayer.app.playlist.entity.PlaylistSong;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    List<PlaylistSong> findByPlaylistIdOrderByPositionAsc(Long playlistId);

    Optional<PlaylistSong> findByPlaylistIdAndSongId(Long playlistId, Long songId);

    boolean existsByPlaylistIdAndSongId(Long playlistId, Long songId);

    @Query("SELECT COUNT(ps) FROM PlaylistSong ps WHERE ps.playlist.id = :playlistId")
    int countByPlaylistId(@Param("playlistId") Long playlistId);

    @Query("SELECT COALESCE(MAX(ps.position), 0) FROM PlaylistSong ps WHERE ps.playlist.id = :playlistId")
    int findMaxPositionByPlaylistId(@Param("playlistId") Long playlistId);

    void deleteByPlaylistIdAndSongId(Long playlistId, Long songId);
}