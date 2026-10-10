package com.example.ecommerceelectroniccomponentsbackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDTO {
    @NotNull
    private Long orderId;

    @NotNull
    private BigDecimal amount;

    // simple field for payment method placeholder
    private String method;
}
