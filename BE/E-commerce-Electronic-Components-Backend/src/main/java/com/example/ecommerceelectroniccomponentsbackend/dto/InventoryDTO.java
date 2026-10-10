package com.example.ecommerceelectroniccomponentsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Integer minQuantity;
    private String warehouse;
    private Boolean isLowStock;
    private LocalDateTime lastUpdated;
}
