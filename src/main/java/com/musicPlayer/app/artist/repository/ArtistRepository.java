package com.musicPlayer.app.artist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicPlayer.app.artist.entity.Artist;

import java.util.List;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {

    @Query("SELECT a FROM Artist a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Artist> search(@Param("query") String query, Pageable pageable);

    Page<Artist> findByGenre(String genre, Pageable pageable);

    List<Artist> findTop10ByOrderByMonthlyListenersDesc();

    @Modifying
    @Query("UPDATE Artist a SET a.followerCount = a.followerCount + 1 WHERE a.id = :id")
    void incrementFollowerCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Artist a SET a.followerCount = a.followerCount - 1 WHERE a.id = :id AND a.followerCount > 0")
    void decrementFollowerCount(@Param("id") Long id);

    @Query("SELECT a FROM Artist a JOIN User u ON a MEMBER OF u.followedArtists WHERE u.id = :userId")
    List<Artist> findFollowedArtistsByUser(@Param("userId") Long userId);
}