package com.example.ecommerceelectroniccomponentsbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ResetPasswordRequest {


    private String confirmPassword;
    @NotBlank(message = "Confirm password is required")

    private String newPassword;
    @Size(min = 6, message = "Password must be at least 6 characters")
    @NotBlank(message = "New password is required")
    @NotBlank(message = "Token is required")
    private String token;

}



