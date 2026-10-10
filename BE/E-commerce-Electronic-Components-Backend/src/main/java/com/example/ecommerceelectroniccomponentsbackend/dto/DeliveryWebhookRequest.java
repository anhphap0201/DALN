package com.example.ecommerceelectroniccomponentsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryWebhookRequest {

    /** Order reference: orderNumber or lockerOrderId */
    private String orderReference;

    /** Delivery status: PENDING, CONFIRMED, SHIPPED, IN_TRANSIT, DELIVERED, DELIVERED_TO_LOCKER, PICKED_UP, COMPLETED, CANCELLED */
    private String deliveryStatus;

    /** OTP for sender to drop off package */
    private String senderOTP;

    /** OTP for recipient to pick up package */
    private String recipientOTP;

    /** Locker compartment ID (if applicable) */
    private String compartmentId;
}
