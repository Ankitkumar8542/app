package com.musicPlayer.app.upload.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.musicPlayer.app.common.constants.AppConstants;
import com.musicPlayer.app.common.response.ApiResponse;
import com.musicPlayer.app.upload.service.CloudinaryUploadService;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "Upload", description = "Standalone file upload endpoints")
public class UploadController {

    private final CloudinaryUploadService uploadService;

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upload audio file to Cloudinary (Admin)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadAudio(
            @RequestPart("file") MultipartFile file) {
        CloudinaryUploadService.UploadResult result = uploadService.uploadAudio(file);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "url", result.url(),
                "publicId", result.publicId(),
                "durationSeconds", result.durationSeconds() != null ? result.durationSeconds() : 0
        )));
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upload image file to Cloudinary (Admin)")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "general") String folder) {
        String targetFolder = switch (folder.toLowerCase()) {
            case "avatar" -> AppConstants.CLOUDINARY_AVATARS_FOLDER;
            case "song" -> AppConstants.CLOUDINARY_IMAGES_FOLDER;
            default -> AppConstants.CLOUDINARY_IMAGES_FOLDER;
        };
        CloudinaryUploadService.UploadResult result = uploadService.uploadImage(file, targetFolder);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "url", result.url(),
                "publicId", result.publicId()
        )));
    }
}