package com.example.ecommerceelectroniccomponentsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductWithVariantsDTO {
    private Long id;
    private String name;
    private String slug;
    private Double avgRating;
    private Integer soldQuantity;
    
    // Price range from variants
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minDiscountPrice;
    private BigDecimal maxDiscountPrice;
    private String defaultImageUrl;
    private Integer totalStock;
    private Boolean hasDiscount;
    
    private List<CategoryDTO> categories = new ArrayList<>();
    private List<ProductVariantDTO> variants = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
