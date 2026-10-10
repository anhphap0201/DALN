package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.CreateOrderRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.OrderDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import com.example.ecommerceelectroniccomponentsbackend.repository.OrderRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IMomoService;
import com.example.ecommerceelectroniccomponentsbackend.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/momo")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class MomoController {

    private final IMomoService momoService;
    private final IOrderService orderService;
    private final OrderRepository orderRepository;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getName());
    }

    /**
     * Create order and initiate MoMo payment
     * POST /api/momo/create
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createMomoPayment(@RequestBody CreateOrderRequest request) {
        try {
            Long userId = getCurrentUserId();

            // Force payment method to MOMO
            request.setPaymentMethod("MOMO");

            // Create the order first
            OrderDTO orderDTO = orderService.createOrderFromCart(userId, request);

            // Get the saved order entity
            Order order = orderRepository.findById(orderDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Order not found after creation"));

            // Create MoMo payment request
            Map<String, Object> momoResult = momoService.createPaymentRequest(order);

            // Add order info to result
            momoResult.put("orderId", orderDTO.getId());
            momoResult.put("orderNumber", order.getOrderNumber());

            return ResponseEntity.ok(momoResult);
        } catch (Exception e) {
            log.error("Error creating MoMo payment", e);
            Map<String, Object> error = new HashMap<>();
            error.put("resultCode", -1);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * MoMo IPN (Instant Payment Notification) callback
     * POST /api/momo/ipn
     * This is called by MoMo server - no auth required
     */
    @PostMapping("/ipn")
    public ResponseEntity<Map<String, Object>> momoIpnCallback(@RequestBody Map<String, String> params) {
        log.info("MoMo IPN callback received");
        Map<String, Object> response = new HashMap<>();

        boolean success = momoService.processIpnCallback(params);
        if (success) {
            response.put("resultCode", 0);
            response.put("message", "OK");
        } else {
            response.put("resultCode", 1);
            response.put("message", "Failed to process IPN");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * MoMo return URL handler - user is redirected here after payment
     * GET /api/momo/return
     * No auth required since MoMo redirects with query params
     */
    @GetMapping("/return")
    public ResponseEntity<Map<String, Object>> momoReturn(@RequestParam Map<String, String> params) {
        log.info("MoMo return received with params: {}", params);
        Map<String, Object> result = momoService.processReturn(params);
        return ResponseEntity.ok(result);
    }
}
