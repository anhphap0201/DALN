package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IProductService {

    ProductDTO createProduct(ProductDTO dto);

    List<ProductDTO> findAll();

    Optional<ProductDTO> findById(Long id);

    ProductDTO updateProductById(Long id, ProductDTO updatedDTO);

    boolean deleteProductById(Long id);

    List<ProductDTO> searchProductsByName(String name);

    List<ProductDTO> findByCategoryName(String categoryName);

    List<ProductDTO> findByCategoryId(Long categoryId);

    List<ProductDTO> findByCategorySlug(String slug);

    Page<ProductDTO> findByMinRating(Double minRating, Pageable pageable);

    Page<ProductDTO> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Page<ProductDTO> findInStockProducts(Pageable pageable);

    Page<ProductDTO> findProductsWithDiscount(Pageable pageable);
}

