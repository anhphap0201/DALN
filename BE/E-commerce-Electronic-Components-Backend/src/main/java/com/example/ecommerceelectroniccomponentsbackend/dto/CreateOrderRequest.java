package com.example.ecommerceelectroniccomponentsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    private String paymentMethod;
    private String shippingMethod;
    private BigDecimal shippingFee;
    private String note;
    private String voucherCode;
    private BigDecimal discount;

    // Smart Locker fields
    private String lockerId;

    // Customer & Shipping information (for Guest checkout or address override)
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String province;
    private String ward;
    private String provinceCode;
    private String wardCode;

    // Items list for Guest checkout
    private List<OrderItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {
        private Long productVariantId;
        private Integer quantity;
    }
}
