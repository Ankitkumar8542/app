package com.musicPlayer.app.song.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicPlayer.app.song.entity.Song;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long>, JpaSpecificationExecutor<Song> {

    Page<Song> findByArtistIdAndStatus(Long artistId, Song.SongStatus status, Pageable pageable);

    Page<Song> findByAlbumIdAndStatus(Long albumId, Song.SongStatus status, Pageable pageable);

    Page<Song> findByStatus(Song.SongStatus status, Pageable pageable);

    @Query("SELECT s FROM Song s WHERE s.status = 'ACTIVE' ORDER BY s.playCount DESC")
    List<Song> findTopByPlayCount(Pageable pageable);

    @Query("SELECT s FROM Song s WHERE s.status = 'ACTIVE' ORDER BY s.createdAt DESC")
    List<Song> findLatestSongs(Pageable pageable);

    @Query("SELECT s FROM Song s JOIN s.categories c WHERE c.id = :categoryId AND s.status = 'ACTIVE'")
    Page<Song> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT s FROM Song s WHERE " +
           "(LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.artist.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND s.status = 'ACTIVE'")
    Page<Song> search(@Param("query") String query, Pageable pageable);

    @Modifying
    @Query("UPDATE Song s SET s.playCount = s.playCount + 1 WHERE s.id = :id")
    void incrementPlayCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Song s SET s.likeCount = s.likeCount + 1 WHERE s.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Song s SET s.likeCount = s.likeCount - 1 WHERE s.id = :id AND s.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    @Query("SELECT s FROM Song s JOIN User u ON s MEMBER OF u.likedSongs WHERE u.id = :userId")
    Page<Song> findLikedSongsByUser(@Param("userId") Long userId, Pageable pageable);

    Long countByStatus(Song.SongStatus status);
}