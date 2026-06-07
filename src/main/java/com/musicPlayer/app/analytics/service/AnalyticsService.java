package com.musicPlayer.app.analytics.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.musicPlayer.app.artist.repository.ArtistRepository;

import com.musicPlayer.app.user.repository.UserRepository;
import com.musicPlayer.app.song.entity.Song;
import com.musicPlayer.app.song.repository.SongRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;

    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalSongs(songRepository.countByStatus(Song.SongStatus.ACTIVE));
        stats.setTotalUsers(userRepository.count());
        stats.setTotalArtists(artistRepository.count());
        stats.setPremiumUsers(userRepository.countPremiumUsers());

        stats.setTopSongs(songRepository.findTopByPlayCount(PageRequest.of(0, 10))
                .stream().map(s -> {
                    SongStats ss = new SongStats();
                    ss.setId(s.getId());
                    ss.setTitle(s.getTitle());
                    ss.setArtistName(s.getArtist() != null ? s.getArtist().getName() : "Unknown");
                    ss.setPlayCount(s.getPlayCount());
                    ss.setCoverImage(s.getCoverImage());
                    return ss;
                }).collect(Collectors.toList()));

        stats.setTopArtists(artistRepository.findTop10ByOrderByMonthlyListenersDesc()
                .stream().map(a -> {
                    ArtistStats as = new ArtistStats();
                    as.setId(a.getId());
                    as.setName(a.getName());
                    as.setMonthlyListeners(a.getMonthlyListeners());
                    as.setImageUrl(a.getImageUrl());
                    return as;
                }).collect(Collectors.toList()));

        return stats;
    }

    @Data
    public static class DashboardStats {
        private Long totalSongs;
        private Long totalUsers;
        private Long totalArtists;
        private Long premiumUsers;
        private List<SongStats> topSongs;
        private List<ArtistStats> topArtists;
    }

    @Data
    public static class SongStats {
        private Long id;
        private String title;
        private String artistName;
        private Long playCount;
        private String coverImage;
    }

    @Data
    public static class ArtistStats {
        private Long id;
        private String name;
        private Long monthlyListeners;
        private String imageUrl;
    }
}