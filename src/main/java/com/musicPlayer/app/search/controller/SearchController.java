package com.musicPlayer.app.search.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.musicPlayer.app.common.response.ApiResponse;
import com.musicPlayer.app.search.service.SearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Global search across songs, artists, albums and playlists")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @Operation(summary = "Search across all entities")
    public ResponseEntity<ApiResponse<SearchService.SearchResult>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(searchService.search(query, page, size)));
    }
}