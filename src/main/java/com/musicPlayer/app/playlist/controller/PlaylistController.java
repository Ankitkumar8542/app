package com.musicPlayer.app.playlist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.musicPlayer.app.common.response.ApiResponse;
import com.musicPlayer.app.common.response.PageResponse;
import com.musicPlayer.app.playlist.dto.PlaylistDtos;
import com.musicPlayer.app.playlist.service.PlaylistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
@Tag(name = "Playlists", description = "Playlist CRUD and song management")
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping
    @Operation(summary = "Get all public playlists")
    public ResponseEntity<ApiResponse<PageResponse<PlaylistDtos.PlaylistResponse>>> getPublicPlaylists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.getPublicPlaylists(page, size)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's playlists")
    public ResponseEntity<ApiResponse<PageResponse<PlaylistDtos.PlaylistResponse>>> getMyPlaylists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.getMyPlaylists(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get playlist by ID")
    public ResponseEntity<ApiResponse<PlaylistDtos.PlaylistResponse>> getPlaylistById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.getPlaylistById(id)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new playlist")
    public ResponseEntity<ApiResponse<PlaylistDtos.PlaylistResponse>> createPlaylist(
            @RequestPart("data") @Valid PlaylistDtos.CreatePlaylistRequest request,
            @RequestPart(value = "cover", required = false) MultipartFile coverImage) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Playlist created",
                        playlistService.createPlaylist(request, coverImage)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update playlist")
    public ResponseEntity<ApiResponse<PlaylistDtos.PlaylistResponse>> updatePlaylist(
            @PathVariable Long id,
            @RequestPart("data") PlaylistDtos.UpdatePlaylistRequest request,
            @RequestPart(value = "cover", required = false) MultipartFile coverImage) {
        return ResponseEntity.ok(ApiResponse.success("Playlist updated",
                playlistService.updatePlaylist(id, request, coverImage)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete playlist")
    public ResponseEntity<ApiResponse<Void>> deletePlaylist(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.ok(ApiResponse.success("Playlist deleted", null));
    }

    @PostMapping("/{id}/songs")
    @Operation(summary = "Add a song to playlist")
    public ResponseEntity<ApiResponse<PlaylistDtos.PlaylistResponse>> addSong(
            @PathVariable Long id,
            @RequestBody PlaylistDtos.AddSongRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Song added",
                playlistService.addSong(id, request.getSongId(), request.getPosition())));
    }

    @DeleteMapping("/{id}/songs/{songId}")
    @Operation(summary = "Remove a song from playlist")
    public ResponseEntity<ApiResponse<Void>> removeSong(
            @PathVariable Long id,
            @PathVariable Long songId) {
        playlistService.removeSong(id, songId);
        return ResponseEntity.ok(ApiResponse.success("Song removed", null));
    }
}