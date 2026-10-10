package com.example.ecommerceelectroniccomponentsbackend.repository;

import com.example.ecommerceelectroniccomponentsbackend.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
    
    @Query("SELECT c FROM Cart c " +
           "LEFT JOIN FETCH c.cartItems ci " +
           "LEFT JOIN FETCH ci.productVariant pv " +
           "LEFT JOIN FETCH pv.product p " +
           "WHERE c.userId = :userId")
    Optional<Cart> findByUserIdWithDetails(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Cart c " +
           "LEFT JOIN FETCH c.cartItems ci " +
           "LEFT JOIN FETCH ci.productVariant pv " +
           "LEFT JOIN FETCH pv.product p " +
           "WHERE c.id = :cartId")
    Optional<Cart> findByIdWithDetails(@Param("cartId") Long cartId);
    
    boolean existsByUserId(Long userId);
}
