package com.musicPlayer.app.album.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.musicPlayer.app.album.dto.AlbumDtos;
import com.musicPlayer.app.album.service.AlbumService;
import com.musicPlayer.app.common.response.ApiResponse;
import com.musicPlayer.app.common.response.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
@Tag(name = "Albums", description = "Album management")
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping
    @Operation(summary = "Get all albums")
    public ResponseEntity<ApiResponse<PageResponse<AlbumDtos.AlbumResponse>>> getAllAlbums(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(albumService.getAllAlbums(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get album by ID")
    public ResponseEntity<ApiResponse<AlbumDtos.AlbumResponse>> getAlbumById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(albumService.getAlbumById(id)));
    }

    @GetMapping("/artist/{artistId}")
    @Operation(summary = "Get albums by artist")
    public ResponseEntity<ApiResponse<PageResponse<AlbumDtos.AlbumResponse>>> getAlbumsByArtist(
            @PathVariable Long artistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(albumService.getAlbumsByArtist(artistId, page, size)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search albums")
    public ResponseEntity<ApiResponse<PageResponse<AlbumDtos.AlbumResponse>>> searchAlbums(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(albumService.searchAlbums(query, page, size)));
    }

    @GetMapping("/new-releases")
    @Operation(summary = "Get new album releases")
    public ResponseEntity<ApiResponse<List<AlbumDtos.AlbumResponse>>> getNewReleases() {
        return ResponseEntity.ok(ApiResponse.success(albumService.getNewReleases()));
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new album (Admin)")
    public ResponseEntity<ApiResponse<AlbumDtos.AlbumResponse>> createAlbum(
            @RequestPart("data") @Valid AlbumDtos.CreateAlbumRequest request,
            @RequestPart(value = "cover", required = false) MultipartFile coverImage) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Album created", albumService.createAlbum(request, coverImage)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update album (Admin)")
    public ResponseEntity<ApiResponse<AlbumDtos.AlbumResponse>> updateAlbum(
            @PathVariable Long id,
            @RequestPart("data") AlbumDtos.UpdateAlbumRequest request,
            @RequestPart(value = "cover", required = false) MultipartFile coverImage) {
        return ResponseEntity.ok(ApiResponse.success("Album updated",
                albumService.updateAlbum(id, request, coverImage)));
    }
}