package com.example.ecommerceelectroniccomponentsbackend.mapper;

import com.example.ecommerceelectroniccomponentsbackend.dto.CategoryDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Category;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CategoryMapper {

    public CategoryDTO toDTO(Category category) {
        if (category == null) return null;
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setImageUrl(category.getImageUrl());
        dto.setDescription(category.getDescription());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }

    public Category toEntity(CategoryDTO dto) {
        if (dto == null) return null;
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setSlug(dto.getSlug());
        category.setImageUrl(dto.getImageUrl());
        category.setDescription(dto.getDescription());
        return category;
    }

    public List<CategoryDTO> toDTOList(List<Category> categories) {
        List<CategoryDTO> list = new ArrayList<>();
        if (categories != null) categories.forEach(c -> list.add(toDTO(c)));
        return list;
    }

    public List<Category> toEntityList(List<CategoryDTO> dtos) {
        List<Category> list = new ArrayList<>();
        if (dtos != null) dtos.forEach(d -> list.add(toEntity(d)));
        return list;
    }
}
