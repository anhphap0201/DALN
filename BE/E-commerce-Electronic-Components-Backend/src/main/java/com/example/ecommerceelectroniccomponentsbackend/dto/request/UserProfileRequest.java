package com.example.ecommerceelectroniccomponentsbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileRequest {
    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must be less than 255 characters")
    private String fullName;

    @NotBlank(message = "Phone is required")
    @Size(max = 20, message = "Phone must be less than 20 characters")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Province is required")
    @Size(max = 255, message = "Province must be less than 255 characters")
    private String province;

    @NotBlank(message = "Ward is required")
    @Size(max = 255, message = "Ward must be less than 255 characters")
    private String ward;

    @NotBlank(message = "Detailed address is required")
    @Size(max = 500, message = "Detailed address must be less than 500 characters")
    private String detailedAddress;
}
