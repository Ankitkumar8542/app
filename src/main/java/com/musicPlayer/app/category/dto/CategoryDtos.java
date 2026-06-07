package com.musicPlayer.app.category.dto;



import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class CategoryDtos {

    @Data
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String imageUrl;
        private String color;
        private String description;
    }

    @Data
    public static class CreateCategoryRequest {
        @NotBlank(message = "Category name is required")
        private String name;
        private String description;
        private String color;
    }
}