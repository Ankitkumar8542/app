package com.musicPlayer.app.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.musicPlayer.app.common.constants.AppConstants;
import com.musicPlayer.app.common.exception.BadRequestException;
import com.musicPlayer.app.common.exception.ResourceNotFoundException;
import com.musicPlayer.app.common.response.PageResponse;
import com.musicPlayer.app.upload.service.CloudinaryUploadService;
import com.musicPlayer.app.user.dto.UserDtos;
import com.musicPlayer.app.user.entity.User;
import com.musicPlayer.app.user.repository.UserRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CloudinaryUploadService uploadService;

    @Transactional(readOnly = true)
    public UserDtos.UserResponse getCurrentUserProfile() {
        return toResponse(getCurrentUser());
    }

    @Transactional
    public UserDtos.UserResponse updateProfile(UserDtos.UpdateProfileRequest request) {
        User user = getCurrentUser();
        user.setName(request.getName());
        if (request.getCountry() != null) user.setCountry(request.getCountry());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserDtos.UserResponse updateProfileImage(MultipartFile image) {
        User user = getCurrentUser();
        if (user.getProfileImagePublicId() != null) {
            uploadService.deleteResource(user.getProfileImagePublicId(), "image");
        }
        CloudinaryUploadService.UploadResult result = uploadService.uploadImage(
                image, AppConstants.CLOUDINARY_AVATARS_FOLDER);
        user.setProfileImage(result.url());
        user.setProfileImagePublicId(result.publicId());
        return toResponse(userRepository.save(user));
    }

    // Admin methods
    @Transactional(readOnly = true)
    public PageResponse<UserDtos.UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.of(userRepository.findAll(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public UserDtos.UserResponse getUserById(Long id) {
        return toResponse(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id)));
    }

    @Transactional
    public UserDtos.UserResponse adminUpdateUser(Long id, UserDtos.AdminUpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (request.getName() != null) user.setName(request.getName());
        if (request.getRole() != null) user.setRole(User.Role.valueOf(request.getRole()));
        if (request.getStatus() != null) user.setStatus(User.AccountStatus.valueOf(request.getStatus()));
        return toResponse(userRepository.save(user));
    }

    public UserDtos.UserResponse toResponse(User user) {
        UserDtos.UserResponse r = new UserDtos.UserResponse();
        r.setId(user.getId());
        r.setName(user.getName());
        r.setEmail(user.getEmail());
        r.setProfileImage(user.getProfileImage());
        r.setRole(user.getRole().name());
        r.setStatus(user.getStatus().name());
        r.setEmailVerified(user.isEmailVerified());
        r.setCountry(user.getCountry());
        r.setDateOfBirth(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null);
        r.setPremium(user.isPremium());
        r.setPremiumExpiresAt(user.getPremiumExpiresAt() != null ? user.getPremiumExpiresAt().toString() : null);
        r.setMonthlyPlayCount(user.getMonthlyPlayCount());
        r.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        return r;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}