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
import java.math.RoundingMode;
import org.springframework.transaction.annotation.Transactional;
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

    @Override
    @Transactional
    public ProductVariantAdminDTO updateDiscount(Long id, java.math.BigDecimal discountPrice) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy linh kiện với ID: " + id));

        if (discountPrice == null || discountPrice.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            variant.setDiscountPrice(null);
        } else {
            if (discountPrice.compareTo(variant.getPrice()) >= 0) {
                throw new IllegalArgumentException("Giá khuyến mãi phải nhỏ hơn giá gốc (" + variant.getPrice() + ")");
            }
            variant.setDiscountPrice(discountPrice);
        }
        variant.setUpdatedAt(java.time.LocalDateTime.now());
        ProductVariant saved = productVariantRepository.save(variant);
        return convertToAdminDTO(saved);
    }

    @Override
    @Transactional
    public List<ProductVariantAdminDTO> updateBatchDiscount(List<Long> variantIds, Integer discountPercent) {
        if (variantIds == null || variantIds.isEmpty()) {
            return List.of();
        }
        if (discountPercent != null && (discountPercent <= 0 || discountPercent >= 100)) {
            throw new IllegalArgumentException("Phần trăm giảm giá phải từ 1% đến 99%");
        }

        List<ProductVariant> variants = productVariantRepository.findAllById(variantIds);
        for (ProductVariant v : variants) {
            if (discountPercent == null || discountPercent == 0) {
                v.setDiscountPrice(null);
            } else {
                java.math.BigDecimal multiplier = java.math.BigDecimal.valueOf(100 - discountPercent)
                        .divide(java.math.BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                java.math.BigDecimal newDiscountPrice = v.getPrice().multiply(multiplier).setScale(0, RoundingMode.HALF_UP);
                if (newDiscountPrice.compareTo(v.getPrice()) < 0) {
                    v.setDiscountPrice(newDiscountPrice);
                }
            }
            v.setUpdatedAt(java.time.LocalDateTime.now());
        }
        List<ProductVariant> saved = productVariantRepository.saveAll(variants);
        return saved.stream().map(this::convertToAdminDTO).collect(java.util.stream.Collectors.toList());
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
