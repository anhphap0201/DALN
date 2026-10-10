package com.example.ecommerceelectroniccomponentsbackend.dto.response;

import com.example.ecommerceelectroniccomponentsbackend.model.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String email;
    private String fullName;
    private Role role;
}
