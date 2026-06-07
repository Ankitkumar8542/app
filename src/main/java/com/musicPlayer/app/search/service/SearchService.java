package com.musicPlayer.app.search.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.musicPlayer.app.album.dto.AlbumDtos;
import com.musicPlayer.app.album.service.AlbumService;
import com.musicPlayer.app.artist.dto.ArtistDtos;
import com.musicPlayer.app.artist.service.ArtistService;
import com.musicPlayer.app.playlist.dto.PlaylistDtos;
import com.musicPlayer.app.playlist.repository.PlaylistRepository;
import com.musicPlayer.app.song.dto.SongDtos;
import com.musicPlayer.app.song.repository.SongRepository;
import com.musicPlayer.app.song.service.SongService;
import com.musicPlayer.app.user.entity.User;
import com.musicPlayer.app.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SongRepository songRepository;
    private final SongService songService;
    private final ArtistService artistService;
    private final AlbumService albumService;
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public SearchResult search(String query, int page, int size) {
        if (query == null || query.isBlank()) return new SearchResult();

        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        SearchResult result = new SearchResult();
        result.setSongs(songRepository.search(query, pageable).map(s -> songService.toResponse(s, user)).getContent());
        result.setArtists(artistService.searchArtists(query, 0, 5).getContent());
        result.setAlbums(albumService.searchAlbums(query, 0, 5).getContent());
        result.setPlaylists(playlistRepository.searchPublic(query, pageable)
                .map(p -> {
                    PlaylistDtos.PlaylistResponse r = new PlaylistDtos.PlaylistResponse();
                    r.setId(p.getId());
                    r.setName(p.getName());
                    r.setCoverImage(p.getCoverImage());
                    r.setTotalSongs(p.getPlaylistSongs().size());
                    PlaylistDtos.PlaylistResponse.OwnerInfo oi = new PlaylistDtos.PlaylistResponse.OwnerInfo();
                    oi.setId(p.getOwner().getId());
                    oi.setName(p.getOwner().getName());
                    r.setOwner(oi);
                    return r;
                }).getContent());
        result.setQuery(query);
        return result;
    }

    @Data
    public static class SearchResult {
        private String query;
        private List<SongDtos.SongResponse> songs = List.of();
        private List<ArtistDtos.ArtistResponse> artists = List.of();
        private List<AlbumDtos.AlbumResponse> albums = List.of();
        private List<PlaylistDtos.PlaylistResponse> playlists = List.of();
    }

    private User getCurrentUser() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) { return null; }
    }
}