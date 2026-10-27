package com.example.ecommerceelectroniccomponentsbackend.dto.warranty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyClaimResponseDTO {
    private Long id;
    private String claimCode;
    private String serialNumber;
    private String productName;
    private String variantName;
    private Long userId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String issueDescription;
    private String status;
    private String resolutionType;
    private String technicianNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
