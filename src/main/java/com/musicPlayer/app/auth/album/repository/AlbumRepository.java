package com.musicPlayer.app.auth.album.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicPlayer.app.album.entity.Album;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    Page<Album> findByArtistId(Long artistId, Pageable pageable);

    @Query("SELECT a FROM Album a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(a.artist.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Album> search(@Param("query") String query, Pageable pageable);

    List<Album> findTop10ByOrderByCreatedAtDesc();
}