package com.example.ecommerceelectroniccomponentsbackend.service.impl;

import com.example.ecommerceelectroniccomponentsbackend.dto.CartDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.CartItemDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Cart;
import com.example.ecommerceelectroniccomponentsbackend.entity.CartItem;
import com.example.ecommerceelectroniccomponentsbackend.entity.ProductVariant;
import com.example.ecommerceelectroniccomponentsbackend.mapper.CartMapper;
import com.example.ecommerceelectroniccomponentsbackend.repository.CartItemRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.CartRepository;
import com.example.ecommerceelectroniccomponentsbackend.repository.ProductVariantRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.ICartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartMapper cartMapper;

    @Transactional
    public CartDTO getCart(Long userId) {
        log.info("Retrieving cart for user: {}", userId);
        Cart cart = cartRepository.findByUserIdWithDetails(userId)
                .orElseGet(() -> createNewCart(userId));
        return cartMapper.toDTO(cart);
    }

    private Cart createNewCart(Long userId) {
        log.info("Creating new cart for user: {}", userId);
        Cart cart = Cart.builder()
                .userId(userId)
                .build();
        return cartRepository.save(cart);
    }

    @Transactional
    public CartDTO addToCart(Long userId, CartItemDTO cartItemDTO) {
        log.info("Adding item to cart for user: {} - variant: {}", userId, cartItemDTO.getProductVariantId());

        Cart cart = cartRepository.findByUserIdWithDetails(userId)
                .orElseGet(() -> createNewCart(userId));

        ProductVariant variant = productVariantRepository.findById(cartItemDTO.getProductVariantId())
                .orElseThrow(() -> new IllegalArgumentException("Product variant not found"));

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductVariantId(cart.getId(), cartItemDTO.getProductVariantId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + cartItemDTO.getQuantity());
            cartItemRepository.save(item);
            log.info("Updated quantity for variant: {}", cartItemDTO.getProductVariantId());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productVariant(variant)
                    .quantity(cartItemDTO.getQuantity())
                    .price(variant.getPrice())
                    .build();
            cart.getCartItems().add(cartItemRepository.save(newItem));
            log.info("Added new item to cart for variant: {}", cartItemDTO.getProductVariantId());
        }

        return cartMapper.toDTO(cart);
    }

    @Transactional
    public CartDTO updateCartItem(Long userId, Long cartItemId, Integer quantity) {
        log.info("Updating cart item: {} to quantity: {} for user: {}", cartItemId, quantity, userId);

        Cart cart = cartRepository.findByUserIdWithDetails(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }

        if (quantity <= 0) {
            cart.getCartItems().removeIf(item -> item.getId().equals(cartItemId));
            cartItemRepository.delete(cartItem);
            log.info("Deleted cart item: {}", cartItemId);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
            log.info("Updated cart item: {} quantity to: {}", cartItemId, quantity);
        }

        return getFreshCartDTO(cart.getId());
    }

    @Transactional
    public CartDTO removeCartItem(Long userId, Long cartItemId) {
        log.info("Removing item: {} from cart for user: {}", cartItemId, userId);

        Cart cart = cartRepository.findByUserIdWithDetails(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }

        cart.getCartItems().removeIf(item -> item.getId().equals(cartItemId));
        cartItemRepository.delete(cartItem);
        return getFreshCartDTO(cart.getId());
    }

    private CartDTO getFreshCartDTO(Long cartId) {
        Cart refreshedCart = cartRepository.findByIdWithDetails(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        return cartMapper.toDTO(refreshedCart);
    }

    @Transactional
    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);

        Cart cart = cartRepository.findByUserIdWithDetails(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user"));

        cart.getCartItems().clear();
        cartRepository.save(cart);
    }
}
