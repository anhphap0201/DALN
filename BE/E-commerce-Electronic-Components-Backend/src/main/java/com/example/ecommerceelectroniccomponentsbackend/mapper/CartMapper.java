package com.example.ecommerceelectroniccomponentsbackend.mapper;

import com.example.ecommerceelectroniccomponentsbackend.dto.CartDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.CartItemDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Cart;
import com.example.ecommerceelectroniccomponentsbackend.entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartDTO toDTO(Cart cart) {
        if (cart == null) return null;

        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .cartItems(cart.getCartItems().stream()
                        .map(this::cartItemToDTO)
                        .collect(Collectors.toSet()))
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    public Cart toEntity(CartDTO dto) {
        if (dto == null) return null;

        return Cart.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .cartItems(dto.getCartItems().stream()
                        .map(this::cartItemToEntity)
                        .collect(Collectors.toSet()))
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public CartItemDTO cartItemToDTO(CartItem cartItem) {
        if (cartItem == null) return null;

        return CartItemDTO.builder()
                .id(cartItem.getId())
                .productVariantId(cartItem.getProductVariant().getId())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getPrice())
                .totalPrice(cartItem.getTotalPrice())
                .productName(cartItem.getProductVariant().getProduct().getName())
                .variantName(cartItem.getProductVariant().getVariantName())
                .imageUrl(cartItem.getProductVariant().getImageUrl())
                .inStock(cartItem.getProductVariant().getInStock())
                .build();
    }

    public CartItem cartItemToEntity(CartItemDTO dto) {
        if (dto == null) return null;

        CartItem cartItem = new CartItem();
        cartItem.setId(dto.getId());
        cartItem.setQuantity(dto.getQuantity());
        return cartItem;
    }
}
