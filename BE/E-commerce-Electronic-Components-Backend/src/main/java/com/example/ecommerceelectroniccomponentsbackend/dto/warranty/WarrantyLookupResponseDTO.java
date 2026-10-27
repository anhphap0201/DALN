package com.example.ecommerceelectroniccomponentsbackend.dto.warranty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyLookupResponseDTO {
    private String serialNumber;
    private Long productId;
    private String productName;
    private String variantName;
    private String productImageUrl;
    private String orderCode;
    private String customerPhone;
    private String customerName;
    private String status;
    private LocalDateTime activatedAt;
    private LocalDateTime expiresAt;
    private boolean isExpired;
    private long daysRemaining;
    private Integer warrantyMonths;
    private List<WarrantyClaimResponseDTO> claimHistory;
}
