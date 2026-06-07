package com.musicPlayer.app.artist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.musicPlayer.app.artist.dto.ArtistDtos;
import com.musicPlayer.app.artist.service.ArtistService;
import com.musicPlayer.app.common.response.ApiResponse;
import com.musicPlayer.app.common.response.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
@Tag(name = "Artists", description = "Artist management and discovery")
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping
    @Operation(summary = "Get all artists")
    public ResponseEntity<ApiResponse<PageResponse<ArtistDtos.ArtistResponse>>> getAllArtists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(artistService.getAllArtists(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get artist by ID")
    public ResponseEntity<ApiResponse<ArtistDtos.ArtistResponse>> getArtistById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(artistService.getArtistById(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search artists")
    public ResponseEntity<ApiResponse<PageResponse<ArtistDtos.ArtistResponse>>> searchArtists(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(artistService.searchArtists(query, page, size)));
    }

    @GetMapping("/top")
    @Operation(summary = "Get top artists by monthly listeners")
    public ResponseEntity<ApiResponse<List<ArtistDtos.ArtistResponse>>> getTopArtists() {
        return ResponseEntity.ok(ApiResponse.success(artistService.getTopArtists()));
    }

    @GetMapping("/following")
    @Operation(summary = "Get artists followed by current user")
    public ResponseEntity<ApiResponse<List<ArtistDtos.ArtistResponse>>> getFollowedArtists() {
        return ResponseEntity.ok(ApiResponse.success(artistService.getFollowedArtists()));
    }

    @PostMapping("/{id}/follow")
    @Operation(summary = "Follow or unfollow an artist")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleFollow(@PathVariable Long id) {
        boolean following = artistService.toggleFollow(id);
        return ResponseEntity.ok(ApiResponse.success(Map.of("following", following)));
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new artist (Admin)")
    public ResponseEntity<ApiResponse<ArtistDtos.ArtistResponse>> createArtist(
            @RequestPart("data") @Valid ArtistDtos.CreateArtistRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Artist created", artistService.createArtist(request, image)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update artist (Admin)")
    public ResponseEntity<ApiResponse<ArtistDtos.ArtistResponse>> updateArtist(
            @PathVariable Long id,
            @RequestPart("data") ArtistDtos.UpdateArtistRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(ApiResponse.success("Artist updated",
                artistService.updateArtist(id, request, image)));
    }
}