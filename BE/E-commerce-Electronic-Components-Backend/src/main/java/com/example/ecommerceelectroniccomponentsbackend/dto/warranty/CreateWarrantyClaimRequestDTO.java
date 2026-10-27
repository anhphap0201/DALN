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
public class CreateWarrantyClaimRequestDTO {

    @NotBlank(message = "Mã Serial không được để trống")
    private String serialNumber;

    @NotBlank(message = "Tên khách hàng không được để trống")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String customerPhone;

    private String customerEmail;

    @NotBlank(message = "Mô tả lỗi hoặc sự cố gặp phải không được để trống")
    private String issueDescription;
}
