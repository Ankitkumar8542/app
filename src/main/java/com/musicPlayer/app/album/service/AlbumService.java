//package com.musicPlayer.app.album.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.musicPlayer.app.album.dto.AlbumDtos;
//import com.musicPlayer.app.album.entity.Album;
//import com.musicPlayer.app.artist.entity.Artist;
//import com.musicPlayer.app.artist.repository.ArtistRepository;
//import com.musicPlayer.app.auth.album.repository.AlbumRepository;
//import com.musicPlayer.app.common.constants.AppConstants;
//import com.musicPlayer.app.common.exception.ResourceNotFoundException;
//import com.musicPlayer.app.common.response.PageResponse;
//import com.musicPlayer.app.song.repository.SongRepository;
//import com.musicPlayer.app.upload.service.CloudinaryUploadService;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class AlbumService {
//
//    private final AlbumRepository albumRepository;
//    private final ArtistRepository artistRepository;
//    private final SongRepository songRepository;
//    private final CloudinaryUploadService uploadService;
//
//    @Transactional
//    public AlbumDtos.AlbumResponse createAlbum(AlbumDtos.CreateAlbumRequest request, MultipartFile coverImage) {
//        Artist artist = artistRepository.findById(request.getArtistId())
//                .orElseThrow(() -> new ResourceNotFoundException("Artist", request.getArtistId()));
//
//        Album album = Album.builder()
//                .title(request.getTitle())
//                .artist(artist)
//                .description(request.getDescription())
//                .type(request.getType() != null
//                        ? Album.AlbumType.valueOf(request.getType().toUpperCase())
//                        : Album.AlbumType.ALBUM)
//                .build();
//
//        if (request.getReleaseDate() != null) {
//            album.setReleaseDate(LocalDate.parse(request.getReleaseDate()));
//        }
//
//        if (coverImage != null && !coverImage.isEmpty()) {
//            CloudinaryUploadService.UploadResult result = uploadService.uploadImage(
//                    coverImage, AppConstants.CLOUDINARY_IMAGES_FOLDER);
//            album.setCoverImage(result.url());
//            album.setCoverImagePublicId(result.publicId());
//        }
//
//        return toResponse(albumRepository.save(album));
//    }
//
//    @Transactional
//    public AlbumDtos.AlbumResponse updateAlbum(Long id, AlbumDtos.UpdateAlbumRequest request,
//                                                 MultipartFile coverImage) {
//        Album album = getOrThrow(id);
//        if (request.getTitle() != null) album.setTitle(request.getTitle());
//        if (request.getDescription() != null) album.setDescription(request.getDescription());
//        if (request.getType() != null) album.setType(Album.AlbumType.valueOf(request.getType().toUpperCase()));
//        if (request.getReleaseDate() != null) album.setReleaseDate(LocalDate.parse(request.getReleaseDate()));
//
//        if (coverImage != null && !coverImage.isEmpty()) {
//            if (album.getCoverImagePublicId() != null) {
//                uploadService.deleteResource(album.getCoverImagePublicId(), "image");
//            }
//            CloudinaryUploadService.UploadResult result = uploadService.uploadImage(
//                    coverImage, AppConstants.CLOUDINARY_IMAGES_FOLDER);
//            album.setCoverImage(result.url());
//            album.setCoverImagePublicId(result.publicId());
//        }
//        return toResponse(albumRepository.save(album));
//    }
//
//    @Transactional(readOnly = true)
//    public AlbumDtos.AlbumResponse getAlbumById(Long id) {
//        return toResponse(getOrThrow(id));
//    }
//
//    @Transactional(readOnly = true)
//    public PageResponse<AlbumDtos.AlbumResponse> getAllAlbums(int page, int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//        return PageResponse.of(albumRepository.findAll(pageable).map(this::toResponse));
//    }
//
//    @Transactional(readOnly = true)
//    public PageResponse<AlbumDtos.AlbumResponse> getAlbumsByArtist(Long artistId, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("releaseDate").descending());
//        return PageResponse.of(albumRepository.findByArtistId(artistId, pageable).map(this::toResponse));
//    }
//
//    @Transactional(readOnly = true)
//    public PageResponse<AlbumDtos.AlbumResponse> searchAlbums(String query, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        return PageResponse.of(albumRepository.search(query, pageable).map(this::toResponse));
//    }
//
//    @Transactional(readOnly = true)
//    public List<AlbumDtos.AlbumResponse> getNewReleases() {
//        return albumRepository.findTop10ByOrderByCreatedAtDesc()
//                .stream().map(this::toResponse).collect(Collectors.toList());
//    }
//
//    private AlbumDtos.AlbumResponse toResponse(Album album) {
//        AlbumDtos.AlbumResponse r = new AlbumDtos.AlbumResponse();
//        r.setId(album.getId());
//        r.setTitle(album.getTitle());
//        r.setCoverImage(album.getCoverImage());
//        r.setReleaseDate(album.getReleaseDate() != null ? album.getReleaseDate().toString() : null);
//        r.setType(album.getType().name());
//        r.setDescription(album.getDescription());
//        r.setTotalTracks(album.getTotalTracks());
//        r.setCreatedAt(album.getCreatedAt() != null ? album.getCreatedAt().toString() : null);
//
//        if (album.getArtist() != null) {
//            AlbumDtos.AlbumResponse.ArtistInfo ai = new AlbumDtos.AlbumResponse.ArtistInfo();
//            ai.setId(album.getArtist().getId());
//            ai.setName(album.getArtist().getName());
//            ai.setImageUrl(album.getArtist().getImageUrl());
//            ai.setVerified(album.getArtist().isVerified());
//            r.setArtist(ai);
//        }
//        return r;
//    }
//
//    private Album getOrThrow(Long id) {
//        return albumRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Album", id));
//    }
//}
package com.musicPlayer.app.album.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.musicPlayer.app.album.dto.AlbumDtos;
import com.musicPlayer.app.album.entity.Album;
import com.musicPlayer.app.artist.entity.Artist;
import com.musicPlayer.app.artist.repository.ArtistRepository;
import com.musicPlayer.app.auth.album.repository.AlbumRepository;
import com.musicPlayer.app.common.constants.AppConstants;
import com.musicPlayer.app.common.exception.ResourceNotFoundException;
import com.musicPlayer.app.common.response.PageResponse;
import com.musicPlayer.app.song.repository.SongRepository;
import com.musicPlayer.app.upload.service.CloudinaryUploadService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final CloudinaryUploadService uploadService;

    @Transactional
    public AlbumDtos.AlbumResponse createAlbum(AlbumDtos.CreateAlbumRequest request, MultipartFile coverImage) {
        Artist artist = artistRepository.findById(request.getArtistId())
                .orElseThrow(() -> new ResourceNotFoundException("Artist", request.getArtistId()));

        Album album = Album.builder()
                .title(request.getTitle())
                .artist(artist)
                .description(request.getDescription())
                .type(request.getType() != null
                        ? Album.AlbumType.valueOf(request.getType().toUpperCase())
                        : Album.AlbumType.ALBUM)
                .build();

        if (request.getReleaseDate() != null) {
            album.setReleaseDate(parseDate(request.getReleaseDate()));
        }

        if (coverImage != null && !coverImage.isEmpty()) {
            CloudinaryUploadService.UploadResult result = uploadService.uploadImage(
                    coverImage, AppConstants.CLOUDINARY_IMAGES_FOLDER);
            album.setCoverImage(result.url());
            album.setCoverImagePublicId(result.publicId());
        }

        return toResponse(albumRepository.save(album));
    }

    @Transactional
    public AlbumDtos.AlbumResponse updateAlbum(Long id, AlbumDtos.UpdateAlbumRequest request,
                                                 MultipartFile coverImage) {
        Album album = getOrThrow(id);
        if (request.getTitle() != null) album.setTitle(request.getTitle());
        if (request.getDescription() != null) album.setDescription(request.getDescription());
        if (request.getType() != null) album.setType(Album.AlbumType.valueOf(request.getType().toUpperCase()));
        if (request.getReleaseDate() != null) album.setReleaseDate(parseDate(request.getReleaseDate()));

        if (coverImage != null && !coverImage.isEmpty()) {
            if (album.getCoverImagePublicId() != null) {
                uploadService.deleteResource(album.getCoverImagePublicId(), "image");
            }
            CloudinaryUploadService.UploadResult result = uploadService.uploadImage(
                    coverImage, AppConstants.CLOUDINARY_IMAGES_FOLDER);
            album.setCoverImage(result.url());
            album.setCoverImagePublicId(result.publicId());
        }
        return toResponse(albumRepository.save(album));
    }

    @Transactional(readOnly = true)
    public AlbumDtos.AlbumResponse getAlbumById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<AlbumDtos.AlbumResponse> getAllAlbums(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.of(albumRepository.findAll(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<AlbumDtos.AlbumResponse> getAlbumsByArtist(Long artistId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("releaseDate").descending());
        return PageResponse.of(albumRepository.findByArtistId(artistId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<AlbumDtos.AlbumResponse> searchAlbums(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.of(albumRepository.search(query, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<AlbumDtos.AlbumResponse> getNewReleases() {
        return albumRepository.findTop10ByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private AlbumDtos.AlbumResponse toResponse(Album album) {
        AlbumDtos.AlbumResponse r = new AlbumDtos.AlbumResponse();
        r.setId(album.getId());
        r.setTitle(album.getTitle());
        r.setCoverImage(album.getCoverImage());
        r.setReleaseDate(album.getReleaseDate() != null ? album.getReleaseDate().toString() : null);
        r.setType(album.getType().name());
        r.setDescription(album.getDescription());
        r.setTotalTracks(album.getTotalTracks());
        r.setCreatedAt(album.getCreatedAt() != null ? album.getCreatedAt().toString() : null);

        if (album.getArtist() != null) {
            AlbumDtos.AlbumResponse.ArtistInfo ai = new AlbumDtos.AlbumResponse.ArtistInfo();
            ai.setId(album.getArtist().getId());
            ai.setName(album.getArtist().getName());
            ai.setImageUrl(album.getArtist().getImageUrl());
            ai.setVerified(album.getArtist().isVerified());
            r.setArtist(ai);
        }
        return r;
    }

    private Album getOrThrow(Long id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album", id));
    }
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        // Try ISO format first: yyyy-MM-dd
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {}
        // Fallback: yyyyMMdd (sent by some browsers' date inputs)
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException ignored) {}
        // Fallback: yyyy/MM/dd
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: '" + dateStr + "'. Use YYYY-MM-DD.");
        }
    }
}
