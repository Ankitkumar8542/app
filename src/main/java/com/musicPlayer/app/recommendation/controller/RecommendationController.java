package com.musicPlayer.app.recommendation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.musicPlayer.app.common.response.ApiResponse;
import com.musicPlayer.app.recommendation.service.RecommendationService;
import com.musicPlayer.app.song.dto.SongDtos;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "Personalized song recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    @Operation(summary = "Get personalized recommendations")
    public ResponseEntity<ApiResponse<List<SongDtos.SongResponse>>> getRecommendations(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getRecommendedSongs(limit)));
    }

    @GetMapping("/similar/{songId}")
    @Operation(summary = "Get songs similar to a given song")
    public ResponseEntity<ApiResponse<List<SongDtos.SongResponse>>> getSimilarSongs(
            @PathVariable Long songId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getSimilarSongs(songId, limit)));
    }

    @GetMapping("/daily-mix")
    @Operation(summary = "Get daily mix playlist")
    public ResponseEntity<ApiResponse<List<SongDtos.SongResponse>>> getDailyMix(
            @RequestParam(defaultValue = "30") int limit) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getDailyMix(limit)));
    }
}