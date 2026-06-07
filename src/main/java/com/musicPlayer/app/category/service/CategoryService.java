package com.musicPlayer.app.category.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.musicPlayer.app.category.dto.CategoryDtos;
import com.musicPlayer.app.category.entity.Category;
import com.musicPlayer.app.category.repository.CategoryRepository;
import com.musicPlayer.app.common.exception.DuplicateResourceException;
import com.musicPlayer.app.common.exception.ResourceNotFoundException;
import com.musicPlayer.app.upload.service.CloudinaryUploadService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CloudinaryUploadService uploadService;

    @Transactional
    public CategoryDtos.CategoryResponse createCategory(CategoryDtos.CreateCategoryRequest request,
                                                         MultipartFile image) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category already exists: " + request.getName());
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .color(request.getColor())
                .build();

        if (image != null && !image.isEmpty()) {
            CloudinaryUploadService.UploadResult result = uploadService.uploadImage(image, "music_player/categories");
            category.setImageUrl(result.url());
        }

        return toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryDtos.CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDtos.CategoryResponse getCategoryById(Long id) {
        return toResponse(categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id)));
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) throw new ResourceNotFoundException("Category", id);
        categoryRepository.deleteById(id);
    }

    private CategoryDtos.CategoryResponse toResponse(Category c) {
        CategoryDtos.CategoryResponse r = new CategoryDtos.CategoryResponse();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setImageUrl(c.getImageUrl());
        r.setColor(c.getColor());
        r.setDescription(c.getDescription());
        return r;
    }
}