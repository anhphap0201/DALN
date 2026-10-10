package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.dto.ProductVariantAdminDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Category;
import com.example.ecommerceelectroniccomponentsbackend.entity.Product;
import com.example.ecommerceelectroniccomponentsbackend.entity.ProductVariant;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductVariantRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements IProductVariantService {
    private final ProductVariantRepository productVariantRepository;

    public List<ProductVariantAdminDTO> getAllVariantsForAdmin() {
        List<ProductVariant> variants = productVariantRepository.findAll();
        return variants.stream()
                .map(this::convertToAdminDTO)
                .collect(Collectors.toList());
    }

    public ProductVariant addVariant(ProductVariant variant) {
        return productVariantRepository.save(variant);
    }

    public ProductVariant updateVariantById(Long id, ProductVariant updatedVariant) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variant not found with id: " + id));
        
        variant.setVariantName(updatedVariant.getVariantName());
        variant.setDescription(updatedVariant.getDescription());
        variant.setSpecifications(updatedVariant.getSpecifications());
        variant.setImageUrl(updatedVariant.getImageUrl());
        variant.setPrice(updatedVariant.getPrice());
        variant.setDiscountPrice(updatedVariant.getDiscountPrice());
        variant.setInStock(updatedVariant.getInStock());
        variant.setIsAvailable(updatedVariant.getIsAvailable());
        
        return productVariantRepository.save(variant);
    }

    public void deleteVariantById(Long id) {
        productVariantRepository.deleteById(id);
    }

    private ProductVariantAdminDTO convertToAdminDTO(ProductVariant variant) {
        ProductVariantAdminDTO dto = new ProductVariantAdminDTO();
        dto.setId(variant.getId());
        dto.setVariantName(variant.getVariantName());
        dto.setDescription(variant.getDescription());
        dto.setSpecifications(variant.getSpecifications());
        dto.setImageUrl(variant.getImageUrl());
        dto.setPrice(variant.getPrice());
        dto.setDiscountPrice(variant.getDiscountPrice());
        dto.setInStock(variant.getInStock());
        dto.setIsAvailable(variant.getIsAvailable());
        dto.setCreatedAt(variant.getCreatedAt());
        dto.setUpdatedAt(variant.getUpdatedAt());

        // Get product information
        Product product = variant.getProduct();
        if (product != null) {
            dto.setProductId(product.getId());
            dto.setProductName(product.getName());
            dto.setProductSlug(product.getSlug());

            // Get category names
            List<String> categoryNames = product.getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.toList());
            dto.setCategoryNames(categoryNames);
        }

        return dto;
    }
}
