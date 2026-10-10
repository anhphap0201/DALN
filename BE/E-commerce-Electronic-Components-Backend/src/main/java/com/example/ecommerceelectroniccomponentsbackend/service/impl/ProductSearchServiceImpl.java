package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.dto.CategoryDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.ProductVariantDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.ProductWithVariantsDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Category;
import com.example.ecommerceelectroniccomponentsbackend.entity.Product;
import com.example.ecommerceelectroniccomponentsbackend.entity.ProductVariant;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchServiceImpl implements IProductSearchService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductWithVariantsDTO> searchProductsByCategoryName(String categoryName) {
        log.info("Searching products by category name: {}", categoryName);
        List<Product> products = productRepository.findByCategoryNameContaining(categoryName);
        log.info("Found {} products for category name: {}", products.size(), categoryName);
        return products.stream()
                .map(this::convertToProductWithVariantsDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductWithVariantsDTO> searchProductsByCategoryId(Long categoryId) {
        log.info("Searching products by category ID: {}", categoryId);
        List<Product> products = productRepository.findByCategoryId(categoryId);
        log.info("Found {} products for category ID: {}", products.size(), categoryId);
        return products.stream()
                .map(this::convertToProductWithVariantsDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductWithVariantsDTO> searchProductsByCategorySlug(String categorySlug) {
        log.info("Searching products by category slug: {}", categorySlug);
        List<Product> products = productRepository.findByCategorySlug(categorySlug);
        log.info("Found {} products for category slug: {}", products.size(), categorySlug);

        // Nếu không tìm thấy với exact match, thử partial match
        if (products.isEmpty()) {
            log.info("No exact match found. Trying partial match for slug: {}", categorySlug);
            products = productRepository.findByCategorySlugContaining(categorySlug);
            log.info("Found {} products with partial slug match", products.size());
        }

        return products.stream()
                .map(this::convertToProductWithVariantsDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductWithVariantsDTO> searchProductsByName(String productName) {
        log.info("Searching products by name: {}", productName);
        List<Product> products = productRepository.findByNameContainingIgnoreCase(productName);
        log.info("Found {} products for name: {}", products.size(), productName);
        return products.stream()
                .map(this::convertToProductWithVariantsDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductWithVariantsDTO getProductById(Long productId) {
        log.info("Getting product by ID: {}", productId);
        return productRepository.findById(productId)
                .map(this::convertToProductWithVariantsDTO)
                .orElse(null);
    }


    public ProductWithVariantsDTO convertToProductWithVariantsDTO(Product product) {
        ProductWithVariantsDTO dto = new ProductWithVariantsDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setAvgRating(product.getAvgRating());
        dto.setSoldQuantity(product.getSoldQuantity());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        // Convert categories
        List<CategoryDTO> categoryDTOs = product.getCategories().stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList());
        dto.setCategories(categoryDTOs);

        // Convert variants
        List<ProductVariantDTO> variantDTOs = product.getVariants().stream()
                .map(this::convertToVariantDTO)
                .collect(Collectors.toList());
        dto.setVariants(variantDTOs);

        // Calculate price range from variants
        List<ProductVariant> variants = product.getVariants();
        if (variants != null && !variants.isEmpty()) {
            // Get min and max prices
            BigDecimal minPrice = variants.stream()
                    .map(ProductVariant::getPrice)
                    .filter(p -> p != null)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            
            BigDecimal maxPrice = variants.stream()
                    .map(ProductVariant::getPrice)
                    .filter(p -> p != null)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            
            dto.setMinPrice(minPrice);
            dto.setMaxPrice(maxPrice);
            
            // Get min and max discount prices (for variants that have discount)
            List<BigDecimal> discountPrices = variants.stream()
                    .map(ProductVariant::getDiscountPrice)
                    .filter(p -> p != null && p.compareTo(BigDecimal.ZERO) > 0)
                    .collect(Collectors.toList());
            
            if (!discountPrices.isEmpty()) {
                dto.setMinDiscountPrice(discountPrices.stream().min(Comparator.naturalOrder()).orElse(null));
                dto.setMaxDiscountPrice(discountPrices.stream().max(Comparator.naturalOrder()).orElse(null));
                dto.setHasDiscount(true);
            } else {
                dto.setHasDiscount(false);
            }
            
            // Get total stock
            Integer totalStock = variants.stream()
                    .map(ProductVariant::getInStock)
                    .filter(s -> s != null)
                    .reduce(0, Integer::sum);
            dto.setTotalStock(totalStock);
            
            // Get default image from first variant that has an image
            String defaultImage = variants.stream()
                    .map(ProductVariant::getImageUrl)
                    .filter(img -> img != null && !img.isEmpty())
                    .findFirst()
                    .orElse(null);
            dto.setDefaultImageUrl(defaultImage);
        } else {
            dto.setMinPrice(BigDecimal.ZERO);
            dto.setMaxPrice(BigDecimal.ZERO);
            dto.setTotalStock(0);
            dto.setHasDiscount(false);
        }

        return dto;
    }


    private CategoryDTO convertToCategoryDTO(Category category) {
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


    private ProductVariantDTO convertToVariantDTO(ProductVariant variant) {
        ProductVariantDTO dto = new ProductVariantDTO();
        dto.setId(variant.getId());
        dto.setProductId(variant.getProduct().getId());
        dto.setProductName(variant.getProduct().getName());
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
        return dto;
    }
}
