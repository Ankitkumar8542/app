package com.musicPlayer.app.history.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.musicPlayer.app.common.response.ApiResponse;
import com.musicPlayer.app.common.response.PageResponse;
import com.musicPlayer.app.history.dto.HistoryDtos;
import com.musicPlayer.app.history.service.HistoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Tag(name = "History", description = "Play history tracking")
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    @Operation(summary = "Get current user's play history")
    public ResponseEntity<ApiResponse<PageResponse<HistoryDtos.HistoryResponse>>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(historyService.getHistory(page, size)));
    }

    @PostMapping
    @Operation(summary = "Record a song play event")
    public ResponseEntity<ApiResponse<Void>> recordPlay(@RequestBody HistoryDtos.RecordPlayRequest request) {
        historyService.recordPlay(request.getSongId(), request.getDurationSeconds(), request.isCompleted());
        return ResponseEntity.ok(ApiResponse.success("Play recorded", null));
    }

    @DeleteMapping
    @Operation(summary = "Clear play history")
    public ResponseEntity<ApiResponse<Void>> clearHistory() {
        historyService.clearHistory();
        return ResponseEntity.ok(ApiResponse.success("History cleared", null));
    }
}