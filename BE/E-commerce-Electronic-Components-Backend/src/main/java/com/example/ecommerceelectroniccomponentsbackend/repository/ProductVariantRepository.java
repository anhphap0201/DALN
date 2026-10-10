package com.example.ecommerceelectroniccomponentsbackend.repository;

import com.example.ecommerceelectroniccomponentsbackend.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);

    @org.springframework.data.jpa.repository.Query("SELECT pv FROM ProductVariant pv JOIN FETCH pv.product p WHERE pv.inStock <= :threshold ORDER BY pv.inStock ASC")
    List<ProductVariant> findLowStockVariants(@org.springframework.data.repository.query.Param("threshold") Integer threshold);
}

