package com.example.ecommerceelectroniccomponentsbackend.dto.warranty;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateWarrantyClaimRequestDTO {

    @NotBlank(message = "Trạng thái không được để trống")
    private String status; // PENDING, PROCESSING, COMPLETED, REJECTED

    private String resolutionType; // REPAIRED, REPLACED_NEW, REJECTED

    private String technicianNotes;
}
