package com.musicPlayer.app.upload.service;

import com.cloudinary.Cloudinary;
import com.musicPlayer.app.common.constants.AppConstants;
import com.musicPlayer.app.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryUploadService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_AUDIO_TYPES = Arrays.asList(
            "audio/mpeg",
            "audio/mp3",
            "audio/wav",
            "audio/ogg",
            "audio/flac",
            "audio/aac",
            "audio/x-m4a"
    );

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    public UploadResult uploadAudio(MultipartFile file) {

        validateAudioFile(file);

        String publicId =
                AppConstants.CLOUDINARY_SONGS_FOLDER + "/" + UUID.randomUUID();

        try {

            Map<String, Object> uploadOptions = Map.of(
                    "public_id", publicId,
                    "resource_type", "video",
                    "folder", AppConstants.CLOUDINARY_SONGS_FOLDER
            );

            Map<String, Object> result =
                    cloudinary.uploader().upload(
                            file.getBytes(),
                            uploadOptions
                    );

            Integer duration =
                    result.get("duration") != null
                            ? ((Number) result.get("duration")).intValue()
                            : 0;

            return new UploadResult(
                    (String) result.get("secure_url"),
                    (String) result.get("public_id"),
                    duration
            );

        } catch (IOException e) {

            log.error("Failed to upload audio file", e);

            throw new RuntimeException(
                    "Failed to upload audio file to Cloudinary",
                    e
            );
        }
    }

    public UploadResult uploadImage(
            MultipartFile file,
            String folder
    ) {

        validateImageFile(file);

        if (file == null || file.isEmpty()) {
            return null;
        }

        String publicId = folder + "/" + UUID.randomUUID();

        try {

            Map<String, Object> uploadOptions = Map.of(
                    "public_id", publicId,
                    "resource_type", "image"
            );

            Map<String, Object> result =
                    cloudinary.uploader().upload(
                            file.getBytes(),
                            uploadOptions
                    );

            return new UploadResult(
                    (String) result.get("secure_url"),
                    (String) result.get("public_id"),
                    null
            );

        } catch (IOException e) {

            log.error("Failed to upload image file", e);

            throw new RuntimeException(
                    "Failed to upload image file to Cloudinary",
                    e
            );
        }
    }

    public void deleteResource(
            String publicId,
            String resourceType
    ) {

        try {

            cloudinary.uploader().destroy(
                    publicId,
                    Map.of("resource_type", resourceType)
            );

        } catch (IOException e) {

            log.error(
                    "Failed to delete resource from Cloudinary: {}",
                    publicId,
                    e
            );
        }
    }

    private void validateAudioFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException(
                    "Audio file is required"
            );
        }

        if (!ALLOWED_AUDIO_TYPES.contains(file.getContentType())) {
            throw new BadRequestException(
                    "Invalid audio format. Allowed formats: MP3, WAV, OGG, FLAC, AAC"
            );
        }

        if (file.getSize() > 50 * 1024 * 1024) {
            throw new BadRequestException(
                    "Audio file size must be less than 50 MB"
            );
        }
    }

    private void validateImageFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return;
        }

        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new BadRequestException(
                    "Invalid image format. Allowed formats: JPEG, JPG, PNG, WEBP, GIF"
            );
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException(
                    "Image file size must be less than 5 MB"
            );
        }
    }

    public record UploadResult(
            String url,
            String publicId,
            Integer durationSeconds
    ) {
    }
}