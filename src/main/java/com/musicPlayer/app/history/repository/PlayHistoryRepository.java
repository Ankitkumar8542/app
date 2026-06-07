package com.musicPlayer.app.history.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.musicPlayer.app.history.entity.PlayHistory;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {

    Page<PlayHistory> findByUserIdOrderByPlayedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT ph.song.id, COUNT(ph) as plays FROM PlayHistory ph " +
           "WHERE ph.user.id = :userId AND ph.playedAt >= :since " +
           "GROUP BY ph.song.id ORDER BY plays DESC")
    List<Object[]> findTopSongsByUser(@Param("userId") Long userId,
                                       @Param("since") LocalDateTime since,
                                       Pageable pageable);

    @Query("SELECT COUNT(ph) FROM PlayHistory ph WHERE ph.user.id = :userId " +
           "AND ph.playedAt >= :startOfMonth")
    Long countMonthlyPlaysByUser(@Param("userId") Long userId,
                                  @Param("startOfMonth") LocalDateTime startOfMonth);

    void deleteByUserIdAndSongId(Long userId, Long songId);
}