package com.example.ecommerceelectroniccomponentsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOrderDTO {
    private Long id;
    private Long userId;
    private String orderNumber;
    private String status;

    // Money
    private BigDecimal totalCost;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discount;

    // Customer info
    private String fullName;
    private String email;
    private String phone;

    // Address
    private String address;
    private String province;
    private String ward;

    // Order details
    private String paymentMethod;
    private String paymentStatus;
    private String shippingMethod;
    private String note;
    private String voucherCode;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    @Builder.Default
    private Set<OrderItemDTO> orderItems = new HashSet<>();
}
