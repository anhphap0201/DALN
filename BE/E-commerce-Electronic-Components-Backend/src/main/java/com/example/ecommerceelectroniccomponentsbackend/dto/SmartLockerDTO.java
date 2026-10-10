package com.example.ecommerceelectroniccomponentsbackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class SmartLockerDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Locker {
        private String lockerId;
        private String locationName;
        private String address;
        private String status;
        private Integer totalCompartments;
        private Integer availableCompartments;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Compartment {
        private String compartmentId;
        private String lockerId;
        private String size;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LockerOrderRequest {
        private String userId;
        private String lockerId;
        private LockerOrderInfo orderInfo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LockerOrderInfo {
        private List<LockerOrderItem> items;
        private Long totalPrice;
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LockerOrderItem {
        private String name;
        private Integer quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LockerOrderResponse {
        private String orderId;
        private String lockerId;
        private String compartmentId;
        private String deliveryStatus;
        private String senderOTP;
        private String recipientOTP;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LockerOrderTracking {
        private String orderId;
        private String userId;
        private String shipperId;
        private String lockerId;
        private String compartmentId;
        private String deliveryStatus;
        private String createdAt;
        private String shippedAt;
        private String pickedUpAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecipientOtpResponse {
        private String orderId;
        private String compartmentId;
        private String recipientOTP;
        private String recipientOTPExpiry;
        private String deliveryStatus;
        private String userId;
        private String shipperId;
        private String lockerId;
        private String createdAt;
        private String shippedAt;
        private String pickedUpAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderInfoDetails {
        private List<LockerOrderItem> items;
        private String notes;
        private Long totalPrice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserOrderResponse {
        private String orderId;
        private String userId;
        private String shipperId;
        private String lockerId;
        private String compartmentId;
        private String deliveryStatus;
        private OrderInfoDetails orderInfo;
        private String createdAt;
        private String shippedAt;
        private String pickedUpAt;
        private String senderOTP;
        private String senderOTPExpiry;
        private String recipientOTP;
        private String recipientOTPExpiry;
    }
}
