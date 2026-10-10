package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.ProductFilterRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.ProductWithVariantsDTO;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public interface IProductFilterService {

    Page<ProductWithVariantsDTO> filterProducts(ProductFilterRequest filter);

    Page<ProductWithVariantsDTO> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice,
                                                      int page, int size, String sortBy);

    Page<ProductWithVariantsDTO> filterInStockProducts(int page, int size, String sortBy);

    Page<ProductWithVariantsDTO> filterProductsWithDiscount(int page, int size, String sortBy);
}

