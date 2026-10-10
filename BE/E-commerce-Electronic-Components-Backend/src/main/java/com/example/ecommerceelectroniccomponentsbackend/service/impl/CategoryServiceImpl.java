package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.dto.CategoryDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Category;
import com.example.ecommerceelectroniccomponentsbackend.mapper.CategoryMapper;
import com.example.ecommerceelectroniccomponentsbackend.repository.CategoryRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryDTO createCategory(CategoryDTO dto) {
        Category category = categoryMapper.toEntity(dto);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toDTO(saved);
    }

    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toDTOList(categories);
    }

    public Optional<CategoryDTO> getCategoryById(Long id) {
        return categoryRepository.findById(id).map(categoryMapper::toDTO);
    }

    public Optional<CategoryDTO> getCategoryByName(String name) {
        return categoryRepository.findByName(name).map(categoryMapper::toDTO);
    }

    public Optional<CategoryDTO> updateCategory(Long id, CategoryDTO updatedDTO) {
        return categoryRepository.findById(id).map(category -> {
            category.setName(updatedDTO.getName());
            category.setSlug(updatedDTO.getSlug());
            category.setImageUrl(updatedDTO.getImageUrl());
            category.setDescription(updatedDTO.getDescription());
            Category saved = categoryRepository.save(category);
            return categoryMapper.toDTO(saved);
        });
    }

    public boolean deleteCategory(Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}
