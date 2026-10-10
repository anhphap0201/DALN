package com.example.ecommerceelectroniccomponentsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterRequest {

    // Lọc theo giá
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // Lọc theo tình trạng kho
    private Boolean inStock;  // true = có hàng, false = hết hàng, null = tất cả

    // Lọc theo đánh giá
    private Double minRating;


    // Lọc theo danh mục
    private String categorySlug;


    // Phân trang
    private Integer page = 0;
    private Integer size = 20;
}
