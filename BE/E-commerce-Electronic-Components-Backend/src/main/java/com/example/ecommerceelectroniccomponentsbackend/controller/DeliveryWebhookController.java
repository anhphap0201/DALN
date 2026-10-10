package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.DeliveryWebhookRequest;
import com.example.ecommerceelectroniccomponentsbackend.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class DeliveryWebhookController {

    private final IOrderService orderService;

    /**
     * Webhook endpoint for a delivery service to push status updates and OTP.
     * POST /api/webhook/delivery
     *
     * Request body:
     * {
     *   "orderReference": "ORD-xxx" or lockerOrderId,
     *   "deliveryStatus": "SHIPPED" | "IN_TRANSIT" | "DELIVERED" | ...,
     *   "senderOTP": "123456",
     *   "recipientOTP": "654321",
     *   "compartmentId": "A-03"
     * }
     */
    @PostMapping("/delivery")
    public ResponseEntity<Map<String, Object>> handleDeliveryUpdate(
            @RequestBody DeliveryWebhookRequest request) {
        log.info("Received delivery webhook: orderRef={}, status={}", 
                request.getOrderReference(), request.getDeliveryStatus());

        Map<String, Object> result = orderService.handleDeliveryWebhook(request);
        return ResponseEntity.ok(result);
    }
}
