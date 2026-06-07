package com.musicPlayer.app.artist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.musicPlayer.app.artist.dto.ArtistDtos;
import com.musicPlayer.app.artist.entity.Artist;
import com.musicPlayer.app.artist.repository.ArtistRepository;
import com.musicPlayer.app.common.constants.AppConstants;
import com.musicPlayer.app.common.exception.BadRequestException;
import com.musicPlayer.app.common.exception.ResourceNotFoundException;
import com.musicPlayer.app.common.response.PageResponse;
import com.musicPlayer.app.upload.service.CloudinaryUploadService;
import com.musicPlayer.app.user.entity.User;
import com.musicPlayer.app.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final CloudinaryUploadService uploadService;

    @Transactional
    public ArtistDtos.ArtistResponse createArtist(ArtistDtos.CreateArtistRequest request, MultipartFile image) {
        Artist artist = Artist.builder()
                .name(request.getName())
                .bio(request.getBio())
                .genre(request.getGenre())
                .country(request.getCountry())
                .build();

        if (image != null && !image.isEmpty()) {
            CloudinaryUploadService.UploadResult result = uploadService.uploadImage(
                    image, AppConstants.CLOUDINARY_IMAGES_FOLDER);
            artist.setImageUrl(result.url());
            artist.setImagePublicId(result.publicId());
        }
        return toResponse(artistRepository.save(artist), getCurrentUser());
    }

    @Transactional
    public ArtistDtos.ArtistResponse updateArtist(Long id, ArtistDtos.UpdateArtistRequest request,
                                                    MultipartFile image) {
        Artist artist = getOrThrow(id);
        if (request.getName() != null) artist.setName(request.getName());
        if (request.getBio() != null) artist.setBio(request.getBio());
        if (request.getGenre() != null) artist.setGenre(request.getGenre());
        if (request.getCountry() != null) artist.setCountry(request.getCountry());

        if (image != null && !image.isEmpty()) {
            if (artist.getImagePublicId() != null) {
                uploadService.deleteResource(artist.getImagePublicId(), "image");
            }
            CloudinaryUploadService.UploadResult result = uploadService.uploadImage(
                    image, AppConstants.CLOUDINARY_IMAGES_FOLDER);
            artist.setImageUrl(result.url());
            artist.setImagePublicId(result.publicId());
        }
        return toResponse(artistRepository.save(artist), getCurrentUser());
    }

    @Transactional(readOnly = true)
    public ArtistDtos.ArtistResponse getArtistById(Long id) {
        return toResponse(getOrThrow(id), getCurrentUser());
    }

    @Transactional(readOnly = true)
    public PageResponse<ArtistDtos.ArtistResponse> getAllArtists(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        User user = getCurrentUser();
        return PageResponse.of(artistRepository.findAll(pageable).map(a -> toResponse(a, user)));
    }

    @Transactional(readOnly = true)
    public PageResponse<ArtistDtos.ArtistResponse> searchArtists(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User user = getCurrentUser();
        return PageResponse.of(artistRepository.search(query, pageable).map(a -> toResponse(a, user)));
    }

    @Transactional(readOnly = true)
    public List<ArtistDtos.ArtistResponse> getTopArtists() {
        User user = getCurrentUser();
        return artistRepository.findTop10ByOrderByMonthlyListenersDesc()
                .stream().map(a -> toResponse(a, user)).collect(Collectors.toList());
    }

    @Transactional
    public boolean toggleFollow(Long artistId) {
        User user = getCurrentUser();
        if (user == null) throw new BadRequestException("Must be logged in");
        Artist artist = getOrThrow(artistId);

        if (user.getFollowedArtists().contains(artist)) {
            user.getFollowedArtists().remove(artist);
            artistRepository.decrementFollowerCount(artistId);
            userRepository.save(user);
            return false;
        } else {
            user.getFollowedArtists().add(artist);
            artistRepository.incrementFollowerCount(artistId);
            userRepository.save(user);
            return true;
        }
    }

    @Transactional(readOnly = true)
    public List<ArtistDtos.ArtistResponse> getFollowedArtists() {
        User user = getCurrentUser();
        if (user == null) throw new BadRequestException("Must be logged in");
        return artistRepository.findFollowedArtistsByUser(user.getId())
                .stream().map(a -> toResponse(a, user)).collect(Collectors.toList());
    }

    private ArtistDtos.ArtistResponse toResponse(Artist artist, User currentUser) {
        ArtistDtos.ArtistResponse r = new ArtistDtos.ArtistResponse();
        r.setId(artist.getId());
        r.setName(artist.getName());
        r.setBio(artist.getBio());
        r.setImageUrl(artist.getImageUrl());
        r.setGenre(artist.getGenre());
        r.setCountry(artist.getCountry());
        r.setMonthlyListeners(artist.getMonthlyListeners());
        r.setFollowerCount(artist.getFollowerCount());
        r.setVerified(artist.isVerified());
        r.setCreatedAt(artist.getCreatedAt() != null ? artist.getCreatedAt().toString() : null);
        if (currentUser != null) {
            r.setFollowing(currentUser.getFollowedArtists().stream()
                    .anyMatch(a -> a.getId().equals(artist.getId())));
        }
        return r;
    }

    private Artist getOrThrow(Long id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist", id));
    }

    private User getCurrentUser() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) { return null; }
    }
}