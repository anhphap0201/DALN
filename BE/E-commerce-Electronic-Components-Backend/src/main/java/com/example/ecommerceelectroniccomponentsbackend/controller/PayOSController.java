package com.example.ecommerceelectroniccomponentsbackend.controller;

import com.example.ecommerceelectroniccomponentsbackend.dto.CreateOrderRequest;
import com.example.ecommerceelectroniccomponentsbackend.dto.OrderDTO;
import com.example.ecommerceelectroniccomponentsbackend.entity.Order;
import com.example.ecommerceelectroniccomponentsbackend.repository.OrderRepository;
import com.example.ecommerceelectroniccomponentsbackend.service.IOrderService;
import com.example.ecommerceelectroniccomponentsbackend.service.IPayOSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.v2.paymentRequests.PaymentLink;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payos")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class PayOSController {

    private final IPayOSService payOSService;
    private final IOrderService orderService;
    private final OrderRepository orderRepository;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getName());
    }

    /**
     * Create order from cart and initiate PayOS payment link
     * POST /api/payos/create
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody CreateOrderRequest request) {
        try {
            Long userId = getCurrentUserId();

            // Set payment method to PAYOS
            request.setPaymentMethod("PAYOS");

            // Create order first
            OrderDTO orderDTO = orderService.createOrderFromCart(userId, request);

            Order order = orderRepository.findById(orderDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng sau khi tạo"));

            // Create PayOS payment link
            Map<String, Object> paymentResult = payOSService.createPaymentLink(order);

            return ResponseEntity.ok(paymentResult);
        } catch (Exception e) {
            log.error("Lỗi khởi tạo thanh toán PayOS", e);
            Map<String, Object> error = new HashMap<>();
            error.put("resultCode", -1);
            error.put("message", e.getMessage() != null ? e.getMessage() : "Lỗi không xác định khi tạo link thanh toán");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Retry PayOS payment for an existing unpaid order
     * POST /api/payos/retry-payment/{orderId}
     */
    @PostMapping("/retry-payment/{orderId}")
    public ResponseEntity<Map<String, Object>> retryPayment(@PathVariable Long orderId) {
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

            Map<String, Object> paymentResult = payOSService.createPaymentLink(order);
            return ResponseEntity.ok(paymentResult);
        } catch (Exception e) {
            log.error("Lỗi thanh toán lại qua PayOS cho đơn hàng #{}", orderId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("resultCode", -1);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * PayOS Webhook receiver
     * POST /api/payos/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> payosWebhook(@RequestBody Object webhookBody) {
        try {
            log.info("Nhận webhook từ PayOS: {}", webhookBody);
            Map<String, Object> result = payOSService.processWebhook(webhookBody);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi xử lý webhook PayOS", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", -1);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get payment link details by orderCode
     * GET /api/payos/info/{orderCode}
     */
    @GetMapping("/info/{orderCode}")
    public ResponseEntity<?> getPaymentInfo(@PathVariable Long orderCode) {
        try {
            PaymentLink info = payOSService.getPaymentLinkInformation(orderCode);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("Lỗi tra cứu thông tin thanh toán PayOS: {}", orderCode, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", -1);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
