package com.musicPlayer.app.song.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.musicPlayer.app.common.response.ApiResponse;
import com.musicPlayer.app.common.response.PageResponse;
import com.musicPlayer.app.song.dto.SongDtos;
import com.musicPlayer.app.song.service.SongService;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@Tag(name = "Songs", description = "Song management and playback")
public class SongController {

    private final SongService songService;

    // ─── Public endpoints ────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get all songs (paginated)")
    public ResponseEntity<ApiResponse<PageResponse<SongDtos.SongResponse>>> getAllSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success(songService.getAllSongs(page, size, sortBy, sortDir)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get song by ID")
    public ResponseEntity<ApiResponse<SongDtos.SongResponse>> getSongById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(songService.getSongById(id)));
    }

    @GetMapping("/top")
    @Operation(summary = "Get top trending songs")
    public ResponseEntity<ApiResponse<List<SongDtos.SongResponse>>> getTopSongs(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(songService.getTopSongs(limit)));
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest uploaded songs")
    public ResponseEntity<ApiResponse<List<SongDtos.SongResponse>>> getLatestSongs(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(songService.getLatestSongs(limit)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search and filter songs")
    public ResponseEntity<ApiResponse<PageResponse<SongDtos.SongResponse>>> searchSongs(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) Long albumId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Boolean premium,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        SongDtos.SongSearchRequest req = new SongDtos.SongSearchRequest();
        req.setQuery(query);
        req.setArtistId(artistId);
        req.setAlbumId(albumId);
        req.setCategoryId(categoryId);
        req.setLanguage(language);
        req.setPremium(premium);
        return ResponseEntity.ok(ApiResponse.success(songService.searchSongs(req, page, size)));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get songs by category")
    public ResponseEntity<ApiResponse<PageResponse<SongDtos.SongResponse>>> getSongsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(songService.getSongsByCategory(categoryId, page, size)));
    }

    // ─── Authenticated endpoints ──────────────────────────────────────────────

    @PostMapping("/{id}/play")
    @Operation(summary = "Record a song play")
    public ResponseEntity<ApiResponse<Void>> recordPlay(@PathVariable Long id) {
        songService.recordPlay(id);
        return ResponseEntity.ok(ApiResponse.success("Play recorded", null));
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Toggle like on a song")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleLike(@PathVariable Long id) {
        boolean liked = songService.toggleLike(id);
        return ResponseEntity.ok(ApiResponse.success(Map.of("liked", liked)));
    }

    @GetMapping("/liked")
    @Operation(summary = "Get current user's liked songs")
    public ResponseEntity<ApiResponse<PageResponse<SongDtos.SongResponse>>> getLikedSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(songService.getLikedSongs(page, size)));
    }

    // ─── Admin endpoints ──────────────────────────────────────────────────────

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upload a new song (Admin)")
    public ResponseEntity<ApiResponse<SongDtos.SongResponse>> uploadSong(
            @RequestPart("audio") MultipartFile audioFile,
            @RequestPart(value = "cover", required = false) MultipartFile coverImage,
            @RequestPart("data") @Valid SongDtos.CreateSongRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Song uploaded successfully",
                        songService.uploadSong(audioFile, coverImage, request)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update song metadata (Admin)")
    public ResponseEntity<ApiResponse<SongDtos.SongResponse>> updateSong(
            @PathVariable Long id,
            @RequestPart("data") SongDtos.UpdateSongRequest request,
            @RequestPart(value = "cover", required = false) MultipartFile coverImage) {
        return ResponseEntity.ok(ApiResponse.success("Song updated",
                songService.updateSong(id, request, coverImage)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a song (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
        return ResponseEntity.ok(ApiResponse.success("Song deleted", null));
    }
}