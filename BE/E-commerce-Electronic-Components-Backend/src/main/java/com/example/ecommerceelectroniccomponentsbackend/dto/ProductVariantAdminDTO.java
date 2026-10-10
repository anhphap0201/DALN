package com.example.ecommerceelectroniccomponentsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantAdminDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productSlug;
    private String variantName;
    private String description;
    private String specifications;
    private String imageUrl; // Image URL for variant
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer inStock;
    private Boolean isAvailable;
    private List<String> categoryNames;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
