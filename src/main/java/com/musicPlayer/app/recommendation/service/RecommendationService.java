package com.musicPlayer.app.recommendation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.musicPlayer.app.history.repository.PlayHistoryRepository;
import com.musicPlayer.app.song.dto.SongDtos;
import com.musicPlayer.app.song.entity.Song;
import com.musicPlayer.app.song.repository.SongRepository;
import com.musicPlayer.app.song.service.SongService;
import com.musicPlayer.app.user.entity.User;
import com.musicPlayer.app.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final SongRepository songRepository;
    private final PlayHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final SongService songService;

    @Transactional(readOnly = true)
    public List<SongDtos.SongResponse> getRecommendedSongs(int limit) {
        User user = getCurrentUser();

        if (user != null) {
            // Get user's most played songs from last 30 days
            LocalDateTime since = LocalDateTime.now().minusDays(30);
            List<Object[]> topSongData = historyRepository.findTopSongsByUser(
                    user.getId(), since, PageRequest.of(0, 5));

            if (!topSongData.isEmpty()) {
                // Get songs from same artists as most played
                List<Long> songIds = topSongData.stream()
                        .map(row -> (Long) row[0]).collect(Collectors.toList());

                List<Song> refSongs = songRepository.findAllById(songIds);
                List<Long> artistIds = refSongs.stream()
                        .map(s -> s.getArtist().getId()).distinct().collect(Collectors.toList());

                List<SongDtos.SongResponse> recommendations = new ArrayList<>();
                for (Long artistId : artistIds) {
                    var artistSongs = songRepository.findByArtistIdAndStatus(
                            artistId, Song.SongStatus.ACTIVE, PageRequest.of(0, 3));
                    artistSongs.getContent().stream()
                            .filter(s -> !songIds.contains(s.getId()))
                            .map(s -> songService.toResponse(s, user))
                            .forEach(recommendations::add);
                    if (recommendations.size() >= limit) break;
                }

                if (!recommendations.isEmpty()) {
                    return recommendations.subList(0, Math.min(limit, recommendations.size()));
                }
            }
        }

        // Fallback: return top/trending songs
        return songRepository.findTopByPlayCount(PageRequest.of(0, limit))
                .stream().map(s -> songService.toResponse(s, user)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDtos.SongResponse> getSimilarSongs(Long songId, int limit) {
        User user = getCurrentUser();
        Song song = songRepository.findById(songId).orElse(null);
        if (song == null) return List.of();

        return songRepository.findByArtistIdAndStatus(
                song.getArtist().getId(), Song.SongStatus.ACTIVE, PageRequest.of(0, limit + 1))
                .getContent().stream()
                .filter(s -> !s.getId().equals(songId))
                .limit(limit)
                .map(s -> songService.toResponse(s, user))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDtos.SongResponse> getDailyMix(int limit) {
        User user = getCurrentUser();
        return songRepository.findTopByPlayCount(PageRequest.of(0, limit))
                .stream().map(s -> songService.toResponse(s, user)).collect(Collectors.toList());
    }

    private User getCurrentUser() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) { return null; }
    }
}