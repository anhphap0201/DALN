package com.example.ecommerceelectroniccomponentsbackend.dto.request;

import lombok.Getter;

@Getter
public class UserCreateRequest {
    private String email;
    private String password;
}
