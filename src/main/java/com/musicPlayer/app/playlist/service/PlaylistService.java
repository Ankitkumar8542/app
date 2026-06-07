package com.musicPlayer.app.playlist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.musicPlayer.app.common.constants.AppConstants;
import com.musicPlayer.app.common.exception.BadRequestException;
import com.musicPlayer.app.common.exception.ForbiddenException;
import com.musicPlayer.app.common.exception.ResourceNotFoundException;
import com.musicPlayer.app.common.response.PageResponse;
import com.musicPlayer.app.playlist.dto.PlaylistDtos;
import com.musicPlayer.app.playlist.entity.Playlist;
import com.musicPlayer.app.playlist.entity.Playlist;
import com.musicPlayer.app.playlist.entity.PlaylistSong;
import com.musicPlayer.app.playlist.repository.PlaylistRepository;
import com.musicPlayer.app.playlist.repository.PlaylistSongRepository;
import com.musicPlayer.app.song.entity.Song;
import com.musicPlayer.app.song.repository.SongRepository;
import com.musicPlayer.app.upload.service.CloudinaryUploadService;
import com.musicPlayer.app.user.entity.User;
import com.musicPlayer.app.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final CloudinaryUploadService uploadService;

    @Transactional
    public PlaylistDtos.PlaylistResponse createPlaylist(PlaylistDtos.CreatePlaylistRequest request,
                                                         MultipartFile coverImage) {
        User user = getCurrentUser();

        if (!user.isPremium()) {
            long count = playlistRepository.countByOwnerId(user.getId());
            if (count >= AppConstants.MAX_FREE_PLAYLISTS) {
                throw new BadRequestException("Free users can only create " +
                        AppConstants.MAX_FREE_PLAYLISTS + " playlists. Upgrade to premium!");
            }
        }

        Playlist playlist = Playlist.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : true)
                .collaborative(request.getCollaborative() != null ? request.getCollaborative() : false)
                .owner(user)
                .build();

        if (coverImage != null && !coverImage.isEmpty()) {
            CloudinaryUploadService.UploadResult result = uploadService.uploadImage(
                    coverImage, AppConstants.CLOUDINARY_IMAGES_FOLDER);
            playlist.setCoverImage(result.url());
            playlist.setCoverImagePublicId(result.publicId());
        }

        return toResponse(playlistRepository.save(playlist), true);
    }

    @Transactional
    public PlaylistDtos.PlaylistResponse updatePlaylist(Long playlistId,
                                                         PlaylistDtos.UpdatePlaylistRequest request,
                                                         MultipartFile coverImage) {
        Playlist playlist = getPlaylistOrThrow(playlistId);
        checkOwnership(playlist);

        if (request.getName() != null) playlist.setName(request.getName());
        if (request.getDescription() != null) playlist.setDescription(request.getDescription());
        if (request.getIsPublic() != null) playlist.setPublic(request.getIsPublic());
        if (request.getCollaborative() != null) playlist.setCollaborative(request.getCollaborative());

        if (coverImage != null && !coverImage.isEmpty()) {
            if (playlist.getCoverImagePublicId() != null) {
                uploadService.deleteResource(playlist.getCoverImagePublicId(), "image");
            }
            CloudinaryUploadService.UploadResult result = uploadService.uploadImage(
                    coverImage, AppConstants.CLOUDINARY_IMAGES_FOLDER);
            playlist.setCoverImage(result.url());
            playlist.setCoverImagePublicId(result.publicId());
        }

        return toResponse(playlistRepository.save(playlist), true);
    }

    @Transactional
    public void deletePlaylist(Long playlistId) {
        Playlist playlist = getPlaylistOrThrow(playlistId);
        checkOwnership(playlist);
        if (playlist.getCoverImagePublicId() != null) {
            uploadService.deleteResource(playlist.getCoverImagePublicId(), "image");
        }
        playlistRepository.delete(playlist);
    }

    @Transactional
    public PlaylistDtos.PlaylistResponse addSong(Long playlistId, Long songId, Integer position) {
    	Playlist playlist = getPlaylistOrThrow(playlistId);
        checkOwnership(playlist);

        if (playlistSongRepository.existsByPlaylistIdAndSongId(playlistId, songId)) {
            throw new BadRequestException("Song is already in this playlist");
        }

        User user = getCurrentUser();
        if (!user.isPremium()) {
            int count = playlistSongRepository.countByPlaylistId(playlistId);
            if (count >= AppConstants.MAX_FREE_PLAYLIST_SONGS) {
                throw new BadRequestException("Free users can only add " +
                        AppConstants.MAX_FREE_PLAYLIST_SONGS + " songs per playlist.");
            }
        }

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song", songId));

        int pos = (position != null) ? position :
                playlistSongRepository.findMaxPositionByPlaylistId(playlistId) + 1;

        PlaylistSong ps = PlaylistSong.builder()
                .playlist(playlist)
                .song(song)
                .position(pos)
                .build();
        playlistSongRepository.save(ps);

        return toResponse(getPlaylistOrThrow(playlistId), true);
    }

    @Transactional
    public void removeSong(Long playlistId, Long songId) {
    	Playlist playlist = getPlaylistOrThrow(playlistId);
        checkOwnership(playlist);
        playlistSongRepository.deleteByPlaylistIdAndSongId(playlistId, songId);
    }

    @Transactional(readOnly = true)
    public PlaylistDtos.PlaylistResponse getPlaylistById(Long playlistId) {
        Playlist playlist = getPlaylistOrThrow(playlistId);
        User user = getCurrentUser();
        if (!playlist.isPublic() && (user == null || !playlist.getOwner().getId().equals(user.getId()))) {
            throw new ForbiddenException("This playlist is private");
        }
        return toResponse(playlist, true);
    }

    @Transactional(readOnly = true)
    public PageResponse<PlaylistDtos.PlaylistResponse> getMyPlaylists(int page, int size) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Playlist> playlists = playlistRepository.findByOwnerId(user.getId(), pageable);
        return PageResponse.of(playlists.map(p -> toResponse(p, false)));
    }

    @Transactional(readOnly = true)
    public PageResponse<PlaylistDtos.PlaylistResponse> getPublicPlaylists(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("followerCount").descending());
        return PageResponse.of(playlistRepository.findByIsPublicTrue(pageable)
                .map(p -> toResponse(p, false)));
    }

    // ---- Mapper ----
    private PlaylistDtos.PlaylistResponse toResponse(Playlist playlist, boolean includeSongs) {
        PlaylistDtos.PlaylistResponse r = new PlaylistDtos.PlaylistResponse();
        r.setId(playlist.getId());
        r.setName(playlist.getName());
        r.setDescription(playlist.getDescription());
        r.setCoverImage(playlist.getCoverImage());
        r.setPublic(playlist.isPublic());
        r.setCollaborative(playlist.isCollaborative());
        r.setFollowerCount(playlist.getFollowerCount());
        r.setCreatedAt(playlist.getCreatedAt() != null ? playlist.getCreatedAt().toString() : null);

        PlaylistDtos.PlaylistResponse.OwnerInfo owner = new PlaylistDtos.PlaylistResponse.OwnerInfo();
        owner.setId(playlist.getOwner().getId());
        owner.setName(playlist.getOwner().getName());
        owner.setProfileImage(playlist.getOwner().getProfileImage());
        r.setOwner(owner);

        if (includeSongs) {
            List<PlaylistDtos.PlaylistResponse.PlaylistSongResponse> songs =
                    playlist.getPlaylistSongs().stream().map(ps -> {
                        PlaylistDtos.PlaylistResponse.PlaylistSongResponse psr =
                                new PlaylistDtos.PlaylistResponse.PlaylistSongResponse();
                        psr.setId(ps.getId());
                        psr.setPosition(ps.getPosition());
                        psr.setAddedAt(ps.getAddedAt() != null ? ps.getAddedAt().toString() : null);

                        Song s = ps.getSong();
                        PlaylistDtos.PlaylistResponse.PlaylistSongResponse.SongInfo si =
                                new PlaylistDtos.PlaylistResponse.PlaylistSongResponse.SongInfo();
                        si.setId(s.getId());
                        si.setTitle(s.getTitle());
                        si.setAudioUrl(s.getAudioUrl());
                        si.setCoverImage(s.getCoverImage());
                        si.setDurationSeconds(s.getDurationSeconds());
                        si.setFormattedDuration(s.getFormattedDuration());
                        si.setPlayCount(s.getPlayCount());

                        if (s.getArtist() != null) {
                            PlaylistDtos.PlaylistResponse.PlaylistSongResponse.SongInfo.ArtistInfo ai =
                                    new PlaylistDtos.PlaylistResponse.PlaylistSongResponse.SongInfo.ArtistInfo();
                            ai.setId(s.getArtist().getId());
                            ai.setName(s.getArtist().getName());
                            ai.setImageUrl(s.getArtist().getImageUrl());
                            si.setArtist(ai);
                        }
                        psr.setSong(si);
                        return psr;
                    }).collect(Collectors.toList());

            r.setSongs(songs);
            r.setTotalSongs(songs.size());

            int totalSecs = playlist.getPlaylistSongs().stream()
                    .mapToInt(ps -> ps.getSong().getDurationSeconds() != null
                            ? ps.getSong().getDurationSeconds() : 0)
                    .sum();
            int hours = totalSecs / 3600;
            int mins = (totalSecs % 3600) / 60;
            r.setTotalDuration(hours > 0 ? hours + "h " + mins + "m" : mins + " min");
        }

        return r;
    }

    private Playlist getPlaylistOrThrow(Long id) {
        return playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist", id));
    }

    private void checkOwnership(Playlist playlist) {
        User user = getCurrentUser();
        if (!playlist.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenException("You don't have permission to modify this playlist");
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}