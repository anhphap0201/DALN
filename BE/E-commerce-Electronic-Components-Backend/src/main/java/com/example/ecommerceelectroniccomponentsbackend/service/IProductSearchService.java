package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.ProductWithVariantsDTO;

import java.util.List;

public interface IProductSearchService {

    List<ProductWithVariantsDTO> searchProductsByCategoryName(String categoryName);

    List<ProductWithVariantsDTO> searchProductsByCategoryId(Long categoryId);

    List<ProductWithVariantsDTO> searchProductsByCategorySlug(String categorySlug);

    List<ProductWithVariantsDTO> searchProductsByName(String productName);

    ProductWithVariantsDTO getProductById(Long productId);

    ProductWithVariantsDTO convertToProductWithVariantsDTO(com.example.ecommerceelectroniccomponentsbackend.entity.Product product);
}

