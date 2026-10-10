package com.example.ecommerceelectroniccomponentsbackend.dto;

import com.example.ecommerceelectroniccomponentsbackend.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String province;
    private String ward;
    private String detailedAddress;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long orderCount;
    private java.math.BigDecimal totalSpent;
}
