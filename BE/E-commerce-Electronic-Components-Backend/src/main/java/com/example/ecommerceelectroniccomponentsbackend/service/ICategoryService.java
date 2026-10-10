package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.CategoryDTO;

import java.util.List;
import java.util.Optional;

public interface ICategoryService {

    CategoryDTO createCategory(CategoryDTO dto);

    List<CategoryDTO> getAllCategories();

    Optional<CategoryDTO> getCategoryById(Long id);

    Optional<CategoryDTO> getCategoryByName(String name);

    Optional<CategoryDTO> updateCategory(Long id, CategoryDTO updatedDTO);

    boolean deleteCategory(Long id);

    boolean existsByName(String name);
}

