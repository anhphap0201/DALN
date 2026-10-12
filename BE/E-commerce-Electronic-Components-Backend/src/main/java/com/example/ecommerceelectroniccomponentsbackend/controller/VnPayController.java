package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.CreateOrderRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.OrderDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import com.example.ecommerceelectroniccomponentsbackend.repository.OrderRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IOrderService;
import com.example.ecommerceelectroniccomponentsbackend.service.IVnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vnpay")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class VnPayController {

    private final IVnPayService vnPayService;
    private final IOrderService orderService;
    private final OrderRepository orderRepository;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getName());
    }

    /**
     * Create order and initiate VNPAY payment
     * POST /api/vnpay/create
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody CreateOrderRequest request,
                                                             HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId();

            // Set payment method to VNPAY
            request.setPaymentMethod("VNPAY");

            // Create order first
            OrderDTO orderDTO = orderService.createOrderFromCart(userId, request);

            Order order = orderRepository.findById(orderDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Order not found after creation"));

            // Create VNPAY payment URL
            Map<String, Object> paymentResult = vnPayService.createPaymentUrl(order, httpRequest);

            paymentResult.put("orderId", orderDTO.getId());
            paymentResult.put("orderNumber", order.getOrderNumber());

            return ResponseEntity.ok(paymentResult);
        } catch (Exception e) {
            log.error("Error creating VNPAY payment", e);
            Map<String, Object> error = new HashMap<>();
            error.put("resultCode", -1);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Retry VNPAY payment for an existing unpaid/awaiting_payment order
     * POST /api/vnpay/retry-payment/{orderId}
     */
    @PostMapping("/retry-payment/{orderId}")
    public ResponseEntity<Map<String, Object>> retryPayment(@PathVariable Long orderId,
                                                            HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId();
            Order order = orderRepository.findByIdAndUserId(orderId, userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            if (order.getStatus() == Order.OrderStatus.CANCELLED) {
                throw new RuntimeException("Đơn hàng đã bị hủy, không thể thanh toán lại");
            }
            if ("PAID".equals(order.getPaymentStatus())) {
                throw new RuntimeException("Đơn hàng đã được thanh toán thành công");
            }

            // Create VNPAY payment URL
            Map<String, Object> paymentResult = vnPayService.createPaymentUrl(order, httpRequest);
            paymentResult.put("orderId", order.getId());
            paymentResult.put("orderNumber", order.getOrderNumber());
            return ResponseEntity.ok(paymentResult);
        } catch (Exception e) {
            log.error("Error retrying VNPAY payment for order: " + orderId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("resultCode", -1);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * VNPAY IPN (Instant Payment Notification) callback
     * GET /api/vnpay/ipn
     */
    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> params) {
        log.info("VNPAY IPN callback received");
        Map<String, String> response = vnPayService.processIpn(params);
        return ResponseEntity.ok(response);
    }

    /**
     * VNPAY return URL handler - client redirect
     * GET /api/vnpay/return
     */
    @GetMapping("/return")
    public ResponseEntity<Map<String, Object>> vnpayReturn(@RequestParam Map<String, String> params) {
        log.info("VNPAY return received with params: {}", params);
        Map<String, Object> result = vnPayService.processReturn(params);
        return ResponseEntity.ok(result);
    }
}
