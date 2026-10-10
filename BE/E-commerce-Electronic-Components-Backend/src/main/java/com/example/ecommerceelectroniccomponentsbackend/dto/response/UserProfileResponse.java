package com.example.ecommerceelectroniccomponentsbackend.dto.response;

import com.example.ecommerceelectroniccomponentsbackend.model.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String province;
    private String ward;
    private String detailedAddress;
    private Role role;
}
