package com.example.ecommerceelectroniccomponentsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {

    private Long id;

    // Optional for guest orders
    private Long userId;

    @Builder.Default
    private String status = "PENDING";

    private BigDecimal totalPrice;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discount;

    // Customer & Shipping information
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String province;
    private String ward;

    @Builder.Default
    private Set<OrderItemDTO> orderItems = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime returnedAt;
    private String returnReason;

    private String orderNumber;
    private String paymentMethod;
    private String paymentStatus;

    // Smart Locker fields
    private String lockerId;
    private String compartmentId;
    private String lockerOrderId;
    private String senderOTP;
    private String recipientOTP;
    private String deliveryStatus;
    private String shippingMethod;
}
