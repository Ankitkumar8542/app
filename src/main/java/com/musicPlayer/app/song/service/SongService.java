package com.musicPlayer.app.song.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.musicPlayer.app.album.entity.Album;
import com.musicPlayer.app.artist.entity.Artist;
import com.musicPlayer.app.artist.repository.ArtistRepository;
import com.musicPlayer.app.auth.album.repository.AlbumRepository;
import com.musicPlayer.app.category.repository.CategoryRepository;
import com.musicPlayer.app.common.constants.AppConstants;
import com.musicPlayer.app.common.exception.BadRequestException;
import com.musicPlayer.app.common.exception.ForbiddenException;
import com.musicPlayer.app.common.exception.ResourceNotFoundException;
import com.musicPlayer.app.common.response.PageResponse;
import com.musicPlayer.app.song.dto.SongDtos;
import com.musicPlayer.app.song.entity.Song;
import com.musicPlayer.app.song.repository.SongRepository;
import com.musicPlayer.app.song.specification.SongSpecification;
import com.musicPlayer.app.upload.service.CloudinaryUploadService;
import com.musicPlayer.app.user.entity.User;
import com.musicPlayer.app.user.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SongService {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CloudinaryUploadService uploadService;

    @Transactional
    public SongDtos.SongResponse uploadSong(MultipartFile audioFile, MultipartFile coverImage,
                                             SongDtos.CreateSongRequest request) {
        Artist artist = artistRepository.findById(request.getArtistId())
                .orElseThrow(() -> new ResourceNotFoundException("Artist", request.getArtistId()));

        // Upload audio
        CloudinaryUploadService.UploadResult audioResult = uploadService.uploadAudio(audioFile);

        // Upload cover image if provided
        String coverImageUrl = null, coverImagePublicId = null;
        if (coverImage != null && !coverImage.isEmpty()) {
            CloudinaryUploadService.UploadResult imageResult = uploadService.uploadImage(
                    coverImage, AppConstants.CLOUDINARY_IMAGES_FOLDER);
            coverImageUrl = imageResult.url();
            coverImagePublicId = imageResult.publicId();
        } else if (artist.getImageUrl() != null) {
            coverImageUrl = artist.getImageUrl();
        }

        Song.SongBuilder builder = Song.builder()
                .title(request.getTitle())
                .audioUrl(audioResult.url())
                .audioPublicId(audioResult.publicId())
                .coverImage(coverImageUrl)
                .coverImagePublicId(coverImagePublicId)
                .durationSeconds(request.getDurationSeconds() != null
                        ? request.getDurationSeconds()
                        : audioResult.durationSeconds())
                .artist(artist)
                .premium(request.isPremium())
                .releaseYear(request.getReleaseYear())
                .language(request.getLanguage())
                .trackNumber(request.getTrackNumber())
                .lyrics(request.getLyrics());

        if (request.getAlbumId() != null) {
            Album album = albumRepository.findById(request.getAlbumId())
                    .orElseThrow(() -> new ResourceNotFoundException("Album", request.getAlbumId()));
            builder.album(album);
        }

        Song song = builder.build();

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            song.setCategories(new HashSet<>(categoryRepository.findAllById(request.getCategoryIds())));
        }
        if (request.getFeaturedArtistIds() != null && !request.getFeaturedArtistIds().isEmpty()) {
            song.setFeaturedArtists(new HashSet<>(artistRepository.findAllById(request.getFeaturedArtistIds())));
        }

        song = songRepository.save(song);
        return toResponse(song, getCurrentUser());
    }

    @Transactional
    public SongDtos.SongResponse updateSong(Long songId, SongDtos.UpdateSongRequest request,
                                             MultipartFile coverImage) {
        Song song = getSongOrThrow(songId);

        if (request.getTitle() != null) song.setTitle(request.getTitle());
        if (request.getLyrics() != null) song.setLyrics(request.getLyrics());
        if (request.getPremium() != null) song.setPremium(request.getPremium());
        if (request.getReleaseYear() != null) song.setReleaseYear(request.getReleaseYear());
        if (request.getLanguage() != null) song.setLanguage(request.getLanguage());
        if (request.getTrackNumber() != null) song.setTrackNumber(request.getTrackNumber());
        if (request.getDurationSeconds() != null) song.setDurationSeconds(request.getDurationSeconds());

        if (request.getArtistId() != null) {
            song.setArtist(artistRepository.findById(request.getArtistId())
                    .orElseThrow(() -> new ResourceNotFoundException("Artist", request.getArtistId())));
        }
        if (request.getAlbumId() != null) {
            song.setAlbum(albumRepository.findById(request.getAlbumId())
                    .orElseThrow(() -> new ResourceNotFoundException("Album", request.getAlbumId())));
        }
        if (request.getCategoryIds() != null) {
            song.setCategories(new HashSet<>(categoryRepository.findAllById(request.getCategoryIds())));
        }

        if (coverImage != null && !coverImage.isEmpty()) {
            if (song.getCoverImagePublicId() != null) {
                uploadService.deleteResource(song.getCoverImagePublicId(), "image");
            }
            CloudinaryUploadService.UploadResult result = uploadService.uploadImage(
                    coverImage, AppConstants.CLOUDINARY_IMAGES_FOLDER);
            song.setCoverImage(result.url());
            song.setCoverImagePublicId(result.publicId());
        }

        return toResponse(songRepository.save(song), getCurrentUser());
    }

    @Transactional
    public void deleteSong(Long songId) {
        Song song = getSongOrThrow(songId);
        uploadService.deleteResource(song.getAudioPublicId(), "video");
        if (song.getCoverImagePublicId() != null) {
            uploadService.deleteResource(song.getCoverImagePublicId(), "image");
        }
        song.setStatus(Song.SongStatus.DELETED);
        songRepository.save(song);
    }

    @Transactional
    public void recordPlay(Long songId) {
        Song song = getSongOrThrow(songId);
        if (song.isPremium()) {
            User currentUser = getCurrentUser();
            if (currentUser != null && !currentUser.isPremium()) {
                throw new ForbiddenException("This song requires a premium subscription");
            }
        }
        songRepository.incrementPlayCount(songId);
    }

    @Transactional
    public boolean toggleLike(Long songId) {
        User user = getCurrentUser();
        if (user == null) throw new BadRequestException("Must be logged in to like songs");

        Song song = getSongOrThrow(songId);
        Set<Song> liked = user.getLikedSongs();

        if (liked.contains(song)) {
            liked.remove(song);
            songRepository.decrementLikeCount(songId);
            userRepository.save(user);
            return false;
        } else {
            liked.add(song);
            songRepository.incrementLikeCount(songId);
            userRepository.save(user);
            return true;
        }
    }

    @Transactional(readOnly = true)
    public SongDtos.SongResponse getSongById(Long songId) {
        return toResponse(getSongOrThrow(songId), getCurrentUser());
    }

    @Transactional(readOnly = true)
    public PageResponse<SongDtos.SongResponse> getAllSongs(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        User user = getCurrentUser();
        return PageResponse.of(songRepository.findByStatus(Song.SongStatus.ACTIVE, pageable)
                .map(s -> toResponse(s, user)));
    }

    @Transactional(readOnly = true)
    public PageResponse<SongDtos.SongResponse> searchSongs(SongDtos.SongSearchRequest request, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Song> spec = SongSpecification.build(request);
        User user = getCurrentUser();
        return PageResponse.of(songRepository.findAll(spec, pageable).map(s -> toResponse(s, user)));
    }

    @Transactional(readOnly = true)
    public List<SongDtos.SongResponse> getTopSongs(int limit) {
        User user = getCurrentUser();
        return songRepository.findTopByPlayCount(PageRequest.of(0, limit))
                .stream().map(s -> toResponse(s, user)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDtos.SongResponse> getLatestSongs(int limit) {
        User user = getCurrentUser();
        return songRepository.findLatestSongs(PageRequest.of(0, limit))
                .stream().map(s -> toResponse(s, user)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<SongDtos.SongResponse> getLikedSongs(int page, int size) {
        User user = getCurrentUser();
        if (user == null) throw new BadRequestException("Must be logged in");
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.of(songRepository.findLikedSongsByUser(user.getId(), pageable)
                .map(s -> toResponse(s, user)));
    }

    @Transactional(readOnly = true)
    public PageResponse<SongDtos.SongResponse> getSongsByCategory(Long categoryId, int page, int size) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.of(songRepository.findByCategoryId(categoryId, pageable)
                .map(s -> toResponse(s, user)));
    }

    // ---- Mapper ----
    public SongDtos.SongResponse toResponse(Song song, User currentUser) {
        SongDtos.SongResponse r = new SongDtos.SongResponse();
        r.setId(song.getId());
        r.setTitle(song.getTitle());
        r.setAudioUrl(song.getAudioUrl());
        r.setCoverImage(song.getCoverImage());
        r.setDurationSeconds(song.getDurationSeconds());
        r.setFormattedDuration(song.getFormattedDuration());
        r.setPlayCount(song.getPlayCount());
        r.setLikeCount(song.getLikeCount());
        r.setTrackNumber(song.getTrackNumber());
        r.setLyrics(song.getLyrics());
        r.setPremium(song.isPremium());
        r.setReleaseYear(song.getReleaseYear());
        r.setLanguage(song.getLanguage());
        r.setStatus(song.getStatus().name());
        r.setCreatedAt(song.getCreatedAt() != null ? song.getCreatedAt().toString() : null);

        if (song.getArtist() != null) {
            SongDtos.SongResponse.ArtistInfo a = new SongDtos.SongResponse.ArtistInfo();
            a.setId(song.getArtist().getId());
            a.setName(song.getArtist().getName());
            a.setImageUrl(song.getArtist().getImageUrl());
            a.setVerified(song.getArtist().isVerified());
            r.setArtist(a);
        }

        if (song.getAlbum() != null) {
            SongDtos.SongResponse.AlbumInfo al = new SongDtos.SongResponse.AlbumInfo();
            al.setId(song.getAlbum().getId());
            al.setTitle(song.getAlbum().getTitle());
            al.setCoverImage(song.getAlbum().getCoverImage());
            al.setReleaseDate(song.getAlbum().getReleaseDate() != null
                    ? song.getAlbum().getReleaseDate().toString() : null);
            r.setAlbum(al);
        }

        r.setCategories(song.getCategories().stream().map(c -> {
            SongDtos.SongResponse.CategoryInfo ci = new SongDtos.SongResponse.CategoryInfo();
            ci.setId(c.getId());
            ci.setName(c.getName());
            ci.setColor(c.getColor());
            return ci;
        }).collect(Collectors.toList()));

        r.setFeaturedArtists(song.getFeaturedArtists().stream().map(fa -> {
            SongDtos.SongResponse.ArtistInfo ai = new SongDtos.SongResponse.ArtistInfo();
            ai.setId(fa.getId());
            ai.setName(fa.getName());
            ai.setImageUrl(fa.getImageUrl());
            ai.setVerified(fa.isVerified());
            return ai;
        }).collect(Collectors.toList()));

        if (currentUser != null) {
            r.setLiked(currentUser.getLikedSongs().stream()
                    .anyMatch(ls -> ls.getId().equals(song.getId())));
        }

        return r;
    }

    private Song getSongOrThrow(Long id) {
        return songRepository.findById(id)
                .filter(s -> s.getStatus() != Song.SongStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Song", id));
    }

    private User getCurrentUser() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}