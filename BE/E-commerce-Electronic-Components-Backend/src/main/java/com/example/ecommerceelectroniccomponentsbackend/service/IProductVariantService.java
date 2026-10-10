package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.ProductVariantAdminDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.ProductVariant;

import java.util.List;

public interface IProductVariantService {

    List<ProductVariantAdminDTO> getAllVariantsForAdmin();

    ProductVariant addVariant(ProductVariant variant);

    ProductVariant updateVariantById(Long id, ProductVariant updatedVariant);

    void deleteVariantById(Long id);
}

