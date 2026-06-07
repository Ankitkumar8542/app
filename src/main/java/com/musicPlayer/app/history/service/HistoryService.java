package com.musicPlayer.app.history.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.musicPlayer.app.common.exception.BadRequestException;
import com.musicPlayer.app.common.response.PageResponse;
import com.musicPlayer.app.history.dto.HistoryDtos;
import com.musicPlayer.app.history.entity.PlayHistory;
import com.musicPlayer.app.history.repository.PlayHistoryRepository;
import com.musicPlayer.app.song.entity.Song;
import com.musicPlayer.app.song.repository.SongRepository;
import com.musicPlayer.app.user.entity.User;
import com.musicPlayer.app.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final PlayHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;

    @Transactional
    public void recordPlay(Long songId, Integer durationSeconds, boolean completed) {
        User user = getCurrentUser();
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new BadRequestException("Song not found"));

        PlayHistory history = PlayHistory.builder()
                .user(user)
                .song(song)
                .playedAt(LocalDateTime.now())
                .completed(completed)
                .playDurationSeconds(durationSeconds)
                .build();
        historyRepository.save(history);
    }

    @Transactional(readOnly = true)
    public PageResponse<HistoryDtos.HistoryResponse> getHistory(int page, int size) {
        User user = getCurrentUser();
        var pageable = PageRequest.of(page, size);
        return PageResponse.of(historyRepository.findByUserIdOrderByPlayedAtDesc(user.getId(), pageable)
                .map(this::toResponse));
    }

    @Transactional
    public void clearHistory() {
        User user = getCurrentUser();
        historyRepository.deleteByUserIdAndSongId(user.getId(), null);
    }

    private HistoryDtos.HistoryResponse toResponse(PlayHistory h) {
        HistoryDtos.HistoryResponse r = new HistoryDtos.HistoryResponse();
        r.setId(h.getId());
        r.setPlayedAt(h.getPlayedAt().toString());
        r.setCompleted(h.isCompleted());
        r.setPlayDurationSeconds(h.getPlayDurationSeconds());

        Song s = h.getSong();
        HistoryDtos.HistoryResponse.SongInfo si = new HistoryDtos.HistoryResponse.SongInfo();
        si.setId(s.getId());
        si.setTitle(s.getTitle());
        si.setCoverImage(s.getCoverImage());
        si.setDurationSeconds(s.getDurationSeconds());
        if (s.getArtist() != null) {
            si.setArtistName(s.getArtist().getName());
        }
        r.setSong(si);
        return r;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}