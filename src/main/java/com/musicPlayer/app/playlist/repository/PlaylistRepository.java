package com.musicPlayer.app.playlist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicPlayer.app.playlist.entity.Playlist;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    Page<Playlist> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Playlist> findByIsPublicTrue(Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.isPublic = true AND " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Playlist> searchPublic(@Param("query") String query, Pageable pageable);

    Long countByOwnerId(Long ownerId);

    @Modifying
    @Query("UPDATE Playlist p SET p.followerCount = p.followerCount + 1 WHERE p.id = :id")
    void incrementFollowerCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Playlist p SET p.followerCount = p.followerCount - 1 WHERE p.id = :id AND p.followerCount > 0")
    void decrementFollowerCount(@Param("id") Long id);
}