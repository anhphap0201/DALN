package com.example.ecommerceelectroniccomponentsbackend.mapper;

import com.example.ecommerceelectroniccomponentsbackend.dto.OrderDTO;
import com.example.ecommerceelectroniccomponentsbackend.dto.OrderItemDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import com.example.ecommerceelectroniccomponentsbackend.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderDTO toDTO(Order order) {
        if (order == null) return null;

        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus().toString())
                .totalPrice(order.getTotalCost()) // Map totalCost to totalPrice for API compatibility
                .orderItems(order.getOrderItems().stream()
                        .map(this::orderItemToDTO)
                        .collect(Collectors.toSet()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .deliveredAt(order.getDeliveredAt())
                .returnedAt(order.getReturnedAt())
                .returnReason(order.getReturnReason())
                .orderNumber(order.getOrderNumber())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .lockerId(order.getLockerId())
                .compartmentId(order.getCompartmentId())
                .lockerOrderId(order.getLockerOrderId())
                .senderOTP(order.getSenderOTP())
                .recipientOTP(order.getRecipientOTP())
                .deliveryStatus(order.getDeliveryStatus())
                .shippingMethod(order.getShippingMethod())
                .build();
    }

    public Order toEntity(OrderDTO dto) {
        if (dto == null) return null;

        return Order.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .status(Order.OrderStatus.valueOf(dto.getStatus()))
                .totalCost(dto.getTotalPrice()) // Map totalPrice to totalCost
                .orderItems(dto.getOrderItems().stream()
                        .map(this::orderItemToEntity)
                        .collect(Collectors.toSet()))
                .deliveryStatus(dto.getDeliveryStatus())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public OrderItemDTO orderItemToDTO(OrderItem orderItem) {
        if (orderItem == null) return null;

        String productName = null;
        String imageUrl = null;
        if (orderItem.getProductVariant() != null) {
            imageUrl = orderItem.getProductVariant().getImageUrl();
            if (orderItem.getProductVariant().getProduct() != null) {
                productName = orderItem.getProductVariant().getProduct().getName();
            }
        }

        return OrderItemDTO.builder()
                .id(orderItem.getId())
                .productVariantId(orderItem.getProductVariant() != null ? orderItem.getProductVariant().getId() : null)
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .totalPrice(orderItem.getTotalPrice())
                .variantName(orderItem.getProductVariant() != null ? orderItem.getProductVariant().getVariantName() : null)
                .productName(productName)
                .productId(orderItem.getProductVariant() != null && orderItem.getProductVariant().getProduct() != null
                        ? orderItem.getProductVariant().getProduct().getId() : null)
                .imageUrl(imageUrl)
                .build();
    }

    public OrderItem orderItemToEntity(OrderItemDTO dto) {
        if (dto == null) return null;

        OrderItem orderItem = new OrderItem();
        orderItem.setId(dto.getId());
        orderItem.setQuantity(dto.getQuantity());
        orderItem.setPrice(dto.getPrice());
        return orderItem;
    }
}
