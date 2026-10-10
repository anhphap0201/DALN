package com.example.ecommerceelectroniccomponentsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
}
