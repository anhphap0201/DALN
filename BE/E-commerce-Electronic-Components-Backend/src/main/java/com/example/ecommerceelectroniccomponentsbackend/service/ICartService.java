package com.example.ecommerceelectroniccomponentsbackend.service;

import com.example.ecommerceelectroniccomponentsbackend.dto.CartDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.CartItemDTO;

public interface ICartService {

    CartDTO getCart(Long userId);

    CartDTO addToCart(Long userId, CartItemDTO cartItemDTO);

    CartDTO updateCartItem(Long userId, Long cartItemId, Integer quantity);

    CartDTO removeCartItem(Long userId, Long cartItemId);

    void clearCart(Long userId);
}

